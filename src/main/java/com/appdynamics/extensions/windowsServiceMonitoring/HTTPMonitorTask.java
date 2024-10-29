package com.appdynamics.extensions.windowsServiceMonitoring;

import com.appdynamics.extensions.AMonitorTaskRunnable;
import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.windowsServiceMonitoring.util.WindowsServiceDataCollector;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.util.AssertUtils;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.windowsServiceMonitoring.util.Constants.*;

/**
 * HTTPMonitorTask is responsible for collecting and reporting metrics for a specified Windows service.
 * It uses the WindowsServiceDataCollector to gather data and MetricWriteHelper to report the metrics.
 */
public class HTTPMonitorTask implements AMonitorTaskRunnable {
    private static final Logger logger = ExtensionsLoggerFactory.getLogger(HTTPMonitorTask.class);
    private MonitorContextConfiguration configuration;
    private MetricWriteHelper metricWriteHelper;
    private Map<String, String> server;
    private String metricPrefix;
    private List<Map<String, ?>> metricList;
    private boolean isRegex;

    public HTTPMonitorTask(MonitorContextConfiguration configuration, MetricWriteHelper metricWriteHelper,
                           Map<String, String> server, boolean isRegex) {
        this.configuration = configuration;
        this.metricWriteHelper = metricWriteHelper;
        this.server = server;
        this.isRegex = isRegex;
        this.metricPrefix = configuration.getMetricPrefix() + DEFAULT_METRIC_SEPARATOR;
        this.metricList = (List<Map<String, ?>>) configuration.getConfigYml().get(METRICS);
        AssertUtils.assertNotNull(this.metricList, "The 'metrics' section in config.yml is either null or empty");
        logger.info("Initialized HTTPMonitorTask for service: {}", server.get(isRegex ? "serviceRegex" : SERVICE_NAME));
    }

    @Override
    public void run() {
        logger.debug("Starting data collection for service: {}", server.get(isRegex ? "serviceRegex" : SERVICE_NAME));

        List<Metric> metrics = new ArrayList<>();

        try {
            WindowsServiceDataCollector dataCollector = new WindowsServiceDataCollector();
            Map<String, Map<String, Object>> allData;

            if (isRegex) {
                String serviceNamePattern = server.get("serviceRegex");
                allData = dataCollector.collectDataUsingRegex(serviceNamePattern);
            } else {
                String serviceName = server.get(SERVICE_NAME);
                allData = dataCollector.collectData(serviceName);
            }

            if (allData != null) {
                for (Map.Entry<String, Map<String, Object>> entry : allData.entrySet()) {
                    String serviceName = entry.getKey();
                    Map<String, Object> data = entry.getValue();

                    logger.debug("Collected data for service {}: {}", serviceName, data);

                    Metric metric1 = new Metric(serviceName, String.valueOf(data.get(SERVICE_UP_TIME_IN_SEC)),
                            metricPrefix + SERVICE_UP_TIME_IN_SEC + DEFAULT_METRIC_SEPARATOR + serviceName);
                    Metric metric2 = new Metric(serviceName, String.valueOf(data.get(SERVICE_STATUS)),
                            metricPrefix + SERVICE_STATUS + DEFAULT_METRIC_SEPARATOR + serviceName);

                    metrics.add(metric1);
                    metrics.add(metric2);

                    logger.info("Collected metrics for service {}: [serviceUpTimeInSec: {}, serviceStatus: {}]",
                            serviceName, data.get(SERVICE_UP_TIME_IN_SEC), data.get(SERVICE_STATUS));
                }
            } else {
                logger.warn("No data collected for service: {}", server.get(isRegex ? "serviceRegex" : SERVICE_NAME));
            }
        } catch (Exception e) {
            logger.error("Error collecting data for service: {}", server.get(isRegex ? "serviceRegex" : SERVICE_NAME), e);
        }

        try {
            metricWriteHelper.transformAndPrintMetrics(metrics);
            logger.info("Metrics successfully reported for service: {}", server.get(isRegex ? "serviceRegex" : SERVICE_NAME));
        } catch (Exception e) {
            logger.error("Error reporting metrics for service: {}", server.get(isRegex ? "serviceRegex" : SERVICE_NAME), e);
        }
    }

    @Override
    public void onTaskComplete() {
        logger.info("All tasks for service {} finished", this.server.get(isRegex ? "serviceRegex" : SERVICE_NAME));
    }
}
