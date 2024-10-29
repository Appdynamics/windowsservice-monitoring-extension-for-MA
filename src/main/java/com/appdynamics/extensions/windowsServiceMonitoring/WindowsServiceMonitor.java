package com.appdynamics.extensions.windowsServiceMonitoring;

import com.appdynamics.extensions.ABaseMonitor;
import com.appdynamics.extensions.TasksExecutionServiceProvider;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.util.AssertUtils;
import org.slf4j.Logger;

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
        AssertUtils.assertNotNull(servicesList, "The 'services' section in config.yml is not initialised");

        // Convert list of service names to a list of maps
        List<Map<String, String>> services = servicesList.stream()
                .map(serviceName -> Collections.singletonMap(SERVICE_NAME, serviceName))
                .collect(Collectors.toList());

        logger.debug("Services to monitor: {}", services);

        /*
         Each task is executed in thread pool, you can have one task to fetch metrics from each artifact concurrently
         */
        for (Map<String, String> service : services) {
            logger.debug("Submitting task for service: {}", service.get(SERVICE_NAME));
            HTTPMonitorTask task = new HTTPMonitorTask(getContextConfiguration(), tasksExecutionServiceProvider.getMetricWriteHelper(), service);
            tasksExecutionServiceProvider.submit("windows-service-monitor", task);
        }

        logger.info("All tasks submitted for WindowsServiceMonitor");
    }

    /**
     * Required by the {@code TaskExecutionServiceProvider} above to know the total number of tasks it needs to wait on.
     * Think of it as a count in the {@code CountDownLatch}
     *
     * @return Number of tasks, i.e. total number of servers to collect metrics from
     */
    @Override
    protected List<Map<String, ?>> getServers() {
        logger.debug("Fetching list of servers from config.yml");
        List<Map<String, ?>> services = (List<Map<String, ?>>) getContextConfiguration().getConfigYml().get("services");
        AssertUtils.assertNotNull(services, "The 'services' section in config.yml is not initialised");
        logger.debug("List of servers: {}", services);
        return services;
    }
}
