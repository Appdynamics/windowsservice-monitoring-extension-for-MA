param (
    [string]$serviceName
)

try {
    # Get the service status
    $service = Get-Service -Name $serviceName -ErrorAction Stop
    $serviceStatus = switch ($service.Status) {
        "Running" { 4 }
        "Stopped" { 1 }
        "Paused" { 7 }
        "StartPending" { 2 }
        "StopPending" { 3 }
        default { 0 }
    }

    # Initialize uptime
    $uptimeInSeconds = 0

    if ($serviceStatus -eq 4) { # Only calculate uptime if the service is running
        # Get the process ID of the service
        $serviceProcessId = (Get-WmiObject -Query "SELECT ProcessId FROM Win32_Service WHERE Name='$serviceName'").ProcessId

        # Get the process start time
        $process = Get-Process -Id $serviceProcessId -ErrorAction Stop
        $startTime = $process.StartTime

        # Calculate the uptime in seconds
        $currentTime = Get-Date
        $uptimeInSeconds = ($currentTime - $startTime).TotalSeconds
    }

    # Output the results as key-value pairs
    Write-Output "serviceStatus=$serviceStatus"
    Write-Output "serviceUpTimeInSec=$([math]::Round($uptimeInSeconds))"
} catch {
    # Output default values if the service does not exist
    Write-Output "serviceStatus=0"
    Write-Output "serviceUpTimeInSec=0"
    Write-Output "Error: $($_.Exception.Message)"
    exit 0
}
