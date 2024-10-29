package com.appdynamics.extensions.windowsServiceMonitoring;

import com.appdynamics.extensions.ABaseMonitor;
import com.appdynamics.extensions.TasksExecutionServiceProvider;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.util.AssertUtils;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.appdynamics.extensions.windowsServiceMonitoring.util.Constants.*;

/**
 * This class will be the main implementation for the extension, the entry point for this class is
 * {@code doRun(TasksExecutionServiceProvider tasksExecutionServiceProvider)}
 * <p>
 * {@code ABaseMonitor} class takes care of all the boiler plate code required for "WindowsServiceMonitor"
 * like initializing {@code MonitorContexConfiguration}, setting the config file from monitor.xml etc.
 * It also internally calls[this call happens everytime the machine agent calls {@code ExtensionMonitor.execute()}]
 * {@code AMonitorJob.run()} -> which in turn calls
 * {@code doRun(TasksExecutionServiceProvider tasksExecutionServiceProvider)} method in this class. {@code AMonitorJob}
 * represents a single run of the extension.
 * <p>
 * {@code windows-service-monitor} (named as "windows-service-monitor") in an extension run(named as "Job").
 * Once all the tasks finish execution, the TaskExecutionServiceProvider(it is the execution service provider
 * for all the tasks in a job), will start DerivedMetricCalculation, print logs related to total metrics
 * sent to the controller in the current job.
 */
public class WindowsServiceMonitor extends ABaseMonitor {
    private static final Logger logger = ExtensionsLoggerFactory.getLogger(WindowsServiceMonitor.class);

    /**
     * Returns the default metric prefix defined in {@code Constants} to be used if metric prefix
     * is missing in config.yml
     * Required for MonitorContextConfiguration initialisation
     *
     * @return {@code String} the default metrics prefix.
     */
    @Override
    protected String getDefaultMetricPrefix() {
        return DEFAULT_METRIC_PREFIX;
    }

    /**
     * Returns the monitor name defined in {@code Constants}
     * Required for MonitorConfiguration initialisation
     *
     * @return {@code String} monitor's name
     */
    @Override
    public String getMonitorName() {
        return MONITOR_NAME;
    }

    /**
     * The entry point for this class.
     * NOTE: The {@code MetricWriteHelper} is initialised internally in {@code AMonitorJob}, but it is exposed through
     * {@code getMetricWriteHelper()} method in {@code TaskExecutionServiceProvider} class.
     *
     * @param tasksExecutionServiceProvider {@code TaskExecutionServiceProvider} is responsible for finishing all the
     *                                      tasks before initialising DerivedMetricsCalculator (It is basically like
     *                                                                          a service that executes the
     *                                      tasks and wait on all of them to finish and does the finish up work).
     */
    @Override
    protected void doRun(TasksExecutionServiceProvider tasksExecutionServiceProvider) {
        logger.info("Starting execution of tasks for WindowsServiceMonitoring");

        // reading a value from the config.yml file
        List<String> servicesList = (List<String>) getContextConfiguration().getConfigYml().get("services");
        List<String> serviceRegexList = (List<String>) getContextConfiguration().getConfigYml().get("serviceRegex");

        // Handle the case where both services and serviceRegex are null or empty
        if ((servicesList == null || servicesList.isEmpty()) && (serviceRegexList == null || serviceRegexList.isEmpty())) {
            logger.warn("Both 'services' and 'serviceRegex' sections in config.yml are empty or not initialised. No tasks will be submitted.");
            return;
        }

        // Convert list of service names to a list of maps
        List<Map<String, String>> services = servicesList != null ? servicesList.stream()
                .map(serviceName -> Collections.singletonMap(SERVICE_NAME, serviceName))
                .collect(Collectors.toList()) : Collections.emptyList();

        List<Map<String, String>> serviceRegexes = serviceRegexList != null ? serviceRegexList.stream()
                .map(regex -> Collections.singletonMap("serviceRegex", regex))
                .collect(Collectors.toList()) : Collections.emptyList();

        logger.debug("Services to monitor: {}", services);
        logger.debug("Service regexes to monitor: {}", serviceRegexes);

        // Submit tasks for exact service names
        for (Map<String, String> service : services) {
            logger.debug("Submitting task for service: {}", service.get(SERVICE_NAME));
            HTTPMonitorTask task = new HTTPMonitorTask(getContextConfiguration(), tasksExecutionServiceProvider.getMetricWriteHelper(), service, false);
            tasksExecutionServiceProvider.submit("windows-service-monitor", task);
        }

        // Submit tasks for service regexes
        for (Map<String, String> serviceRegex : serviceRegexes) {
            logger.debug("Submitting task for service regex: {}", serviceRegex.get("serviceRegex"));
            HTTPMonitorTask task = new HTTPMonitorTask(getContextConfiguration(), tasksExecutionServiceProvider.getMetricWriteHelper(), serviceRegex, true);
            tasksExecutionServiceProvider.submit("windows-service-monitor", task);
        }

        logger.info("All tasks submitted for WindowsServiceMonitor");
    }

    @Override
    protected List<Map<String, ?>> getServers() {
        logger.debug("Fetching list of servers from config.yml");
        List<Map<String, ?>> services = (List<Map<String, ?>>) getContextConfiguration().getConfigYml().get("services");
        List<Map<String, ?>> serviceRegexes = (List<Map<String, ?>>) getContextConfiguration().getConfigYml().get("serviceRegex");
        List<Map<String, ?>> allServices = new ArrayList<>();

        if (services != null) {
            allServices.addAll(services);
        }
        if (serviceRegexes != null) {
            allServices.addAll(serviceRegexes);
        }

        if (allServices.isEmpty()) {
            logger.warn("Both 'services' and 'serviceRegex' sections in config.yml are empty or not initialised.");
        } else {
            logger.debug("List of servers: {}", allServices);
        }

        return allServices;
    }
}