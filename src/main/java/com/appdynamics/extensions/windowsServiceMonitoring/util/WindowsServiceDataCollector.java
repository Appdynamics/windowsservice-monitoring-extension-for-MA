package com.appdynamics.extensions.windowsServiceMonitoring.util;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class WindowsServiceDataCollector {
    private static final Logger logger = ExtensionsLoggerFactory.getLogger(WindowsServiceDataCollector.class);

    private static final String SCRIPT_NAME = "Get-ServiceData.ps1";

    public Map<String, Map<String, Object>> collectData(String serviceName) {
        logger.debug("Starting data collection for service: {}", serviceName);

        Map<String, Map<String, Object>> allData = new HashMap<>();
        try {
            String script = readScriptFromResources(SCRIPT_NAME);
            File tempScriptFile = File.createTempFile("Get-ServiceData", ".ps1");
            try (FileWriter writer = new FileWriter(tempScriptFile)) {
                writer.write(script);
            }

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "powershell.exe", "-File", tempScriptFile.getAbsolutePath(), "-serviceName", serviceName);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                logger.error("PowerShell script exited with code: {}", exitCode);
                logger.error("PowerShell script output: {}", output.toString());
                return null;
            }

            logger.debug("Raw PowerShell script output: {}", output.toString());

            allData.put(serviceName, parseOutput(output.toString()));

            return allData;

        } catch (Exception e) {
            logger.error("Failed to collect data for service: {}", serviceName, e);
            return null;
        }
    }

    public Map<String, Map<String, Object>> collectDataUsingRegex(String serviceNamePattern) {
        logger.debug("Starting data collection for services matching regex: {}", serviceNamePattern);

        Map<String, Map<String, Object>> allData = new HashMap<>();
        try {
            String script = readScriptFromResources(SCRIPT_NAME);
            File tempScriptFile = File.createTempFile("Get-ServiceData", ".ps1");
            try (FileWriter writer = new FileWriter(tempScriptFile)) {
                writer.write(script);
            }

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "powershell.exe", "-File", tempScriptFile.getAbsolutePath(), "-serviceNamePattern", serviceNamePattern);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                logger.error("PowerShell script exited with code: {}", exitCode);
                logger.error("PowerShell script output: {}", output.toString());
                return null;
            }

            logger.debug("Raw PowerShell script output: {}", output.toString());

            String[] serviceOutputs = output.toString().split("SERVICE_DELIMITER");
            for (String serviceOutput : serviceOutputs) {
                Map<String, Object> serviceData = parseOutput(serviceOutput);
                String serviceName = (String) serviceData.get("serviceName");
                if (!serviceData.isEmpty()) {
                    allData.put(serviceName, serviceData);
                }
            }

            return allData;

        } catch (Exception e) {
            logger.error("Failed to collect data for services matching regex: {}", serviceNamePattern, e);
            return null;
        }
    }

    private String readScriptFromResources(String scriptName) throws Exception {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(scriptName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    private Map<String, Object> parseOutput(String output) {
        Map<String, Object> metricData = new HashMap<>();
        String[] lines = output.split("\n");
        for (String line : lines) {
            String[] keyValue = line.split("=");
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                if (key.equals("serviceName")) {
                    metricData.put(key, value);
                } else {
                    try {
                        metricData.put(key, Long.parseLong(value));
                    } catch (NumberFormatException e) {
                        logger.error("Failed to parse value for key {}: {}", key, value, e);
                    }
                }
            }
        }
        return metricData;
    }
}
