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

    public Map<String, Long> collectData(String serviceName) {
        logger.debug("Starting data collection for service: {}", serviceName);

        try {
            // Read the PowerShell script from the resources
            String script = readScriptFromResources(SCRIPT_NAME);

            // Write the script to a temporary file
            File tempScriptFile = File.createTempFile("Get-ServiceData", ".ps1");
            try (FileWriter writer = new FileWriter(tempScriptFile)) {
                writer.write(script);
            }

            // Execute the PowerShell script
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "powershell.exe", "-File", tempScriptFile.getAbsolutePath(), "-serviceName", serviceName);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Read the output
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

            // Log the raw output for debugging
            logger.debug("Raw PowerShell script output: {}", output.toString());

            Map<String, Long> metricData = parseOutput(output.toString());
            logger.debug("Collected data for service {}: {}", serviceName, metricData);
            return metricData;

        } catch (Exception e) {
            logger.error("Failed to collect data for service: {}", serviceName, e);
            return null;
        }
    }

    private String readScriptFromResources(String scriptName) throws Exception {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(scriptName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    private Map<String, Long> parseOutput(String output) {
        Map<String, Long> metricData = new HashMap<>();
        String[] lines = output.split("\n");
        for (String line : lines) {
            String[] keyValue = line.split("=");
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                Long value;
                try {
                    value = Long.parseLong(keyValue[1].trim());
                } catch (NumberFormatException e) {
                    logger.error("Failed to parse value for key {}: {}", key, keyValue[1].trim(), e);
                    continue;
                }
                metricData.put(key, value);
            }
        }
        return metricData;
    }
}
