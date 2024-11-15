/*
 * Copyright (c) 2019 AppDynamics,Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appdynamics.extensions.windowsServiceMonitoring.util;

/**
 * A utility class for storing defaults that are used throughout the project
 */
public class Constants {
    public static final String DEFAULT_METRIC_PREFIX = "Custom Metrics|WindowsServiceMonitorExtension";
    public static final String DEFAULT_METRIC_SEPARATOR = "|";
    public static final String MONITOR_NAME = "WindowsServiceMonitorExtension";
    public static final String METRICS = "metrics";
    public static final String SERVICE_NAME = "serviceName";
    public static final String SERVICE_UP_TIME_IN_SEC = "serviceUpTimeInSec";
    public static final String SERVICE_STATUS = "serviceStatus";
}
