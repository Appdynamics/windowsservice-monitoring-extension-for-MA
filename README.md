# Windows Service Monitoring Extension
## Usage
The Windows Service Monitor Extension can be used to provide metrics related to service time and service uptime.
## Prerequisites
1. Before the extension is installed, the prerequisites mentioned [here](https://community.appdynamics.com/t5/Knowledge-Base/Extensions-Prerequisites-Guide/ta-p/35213) need to be met. Please do not proceed with the extension installation if the specified prerequisites are not met.

2. Download and install [Apache Maven](https://maven.apache.org/) which is configured with `Java 8` to build the extension artifact from source. You can check the java version used in maven using command `mvn -v` or `mvn --version`. If your maven is using some other java version then please download java 8 for your platform and set JAVA_HOME parameter before starting maven.

3. The extension needs to be deployed on the same box as the ones with the windows service to be monitored.

## Installation
1. Clone this repository to your local repository using `git clone <repo-url>` command. 
2. Run `mvn clean install` and find the `WindowsServiceMonitorExtension-VERSION.zip` file in the `target` folder.
3. Unzip and copy that directory to `<MACHINE_AGENT_HOME>/monitors`

Please place the extension in the "__monitors__" directory of your __Machine Agent__ installation directory. Do not place the extension in the "__extensions__" directory of your __Machine Agent__ installation directory.

## Configuration
### config.yml
Configure the extension by editing the `config.yml` file in `<MACHINE_AGENT_HOME>/monitors/WindowsServiceMonitorExtension/`
1. Configure the "tier" under which the metrics need to be reported. This can be done by changing the value of `<TIER ID>` in

     `metricPrefix: "Server|Component:<TIER ID>|Custom Metrics|WindowsServiceMonitorExtension"`

    If SIM is enabled, please use the default metric prefix. metricPrefix: "Custom Metrics|WindowsServiceMonitorExtension|

    More details around metric prefix can be found [here](https://community.appdynamics.com/t5/Knowledge-Base/How-do-I-troubleshoot-missing-custom-metrics-or-extensions/ta-p/28695).

2. Add the names of the services you want to monitor in the config.yml file.  <br/>For example,
 
     ```
     services: ["XblGameSave", "Ifsvc", "Dhcp"]

     ```
3. Configure the encryptionKey for encryptionPasswords(only if password encryption required).<br/>For example,
   ```
   encryptionKey: welcome
   ```
4. Configure the `numberOfThreads` depending on the number of concurrent tasks. For example, if you are monitoring three services, and each task for each service runs as a single thread then use `numberOfThreads: 3`.


## Metrics
The extension provides following metrics

1. Service Status

   1   :  SERVICE_STOPPED

    2   :  SERVICE_START_PENDING

    3   :  SERVICE_STOP_PENDING

    4   :  SERVICE_RUNNING

    5   :  CONTINUE_PENDING

    6   :  PAUSE_PENDING

    7   :  SERVICE_PAUSED

2. Service Uptime In Sec

   Will be 0 if service is not running
    
    

## Credentials Encryption
Please visit [this page](https://community.appdynamics.com/t5/Knowledge-Base/How-to-use-Password-Encryption-with-Extensions/ta-p/29397) to get detailed instructions on password encryption. The steps in this document will guide you through the whole process.

## Extensions Workbench
Workbench is an inbuilt feature provided with each extension in order to assist you to fine tune the extension setup before you actually deploy it on the controller. Please review the following document on [How to use the Extensions WorkBench](https://community.appdynamics.com/t5/Knowledge-Base/How-to-use-the-Extensions-WorkBench/ta-p/30130)

## Troubleshooting
Please follow the steps listed in this [troubleshooting-document](https://community.appdynamics.com/t5/Knowledge-Base/How-to-troubleshoot-missing-custom-metrics-or-extensions-metrics/ta-p/28695) in order to troubleshoot your issue. These are a set of common issues that customers might have faced during the installation of the extension.


## Contributing
Always feel free to fork and contribute any changes directly via [GitHub](https://github.com/Appdynamics/extension-starter).

## Version
Name |	Version
---|---
Extension Version |	1.0
Last Update |	07/10/2024
ChangeList | [ChangeLog](https://github.com/Appdynamics/extension-starter/blob/master/CHANGELOG.md)
