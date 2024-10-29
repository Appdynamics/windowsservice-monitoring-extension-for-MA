param (
    [string]$serviceName,
    [string]$serviceNamePattern
)

try {
    $services = @()
    if ($serviceName) {
        $services = @(Get-Service -Name $serviceName -ErrorAction Stop)
    } elseif ($serviceNamePattern) {
        $services = @(Get-Service | Where-Object { $_.Name -match $serviceNamePattern })
    }

    foreach ($service in $services) {
        $serviceStatus = switch ($service.Status) {
            "Running" { 4 }
            "Stopped" { 1 }
            "Paused" { 7 }
            "StartPending" { 2 }
            "StopPending" { 3 }
            default { 0 }
        }

        $uptimeInSeconds = 0

        if ($serviceStatus -eq 4) {
            $serviceProcessId = (Get-WmiObject -Query "SELECT ProcessId FROM Win32_Service WHERE Name='$($service.Name)'").ProcessId
            $process = Get-Process -Id $serviceProcessId -ErrorAction Stop
            $startTime = $process.StartTime
            $currentTime = Get-Date
            $uptimeInSeconds = ($currentTime - $startTime).TotalSeconds
        }

        Write-Output "serviceName=$($service.Name)"
        Write-Output "serviceStatus=$serviceStatus"
        Write-Output "serviceUpTimeInSec=$([math]::Round($uptimeInSeconds))"
        Write-Output "SERVICE_DELIMITER"
    }
} catch {
    Write-Output "serviceName=$serviceName"
    Write-Output "serviceStatus=0"
    Write-Output "serviceUpTimeInSec=0"
    Write-Output "Error: $($_.Exception.Message)"
    exit 0
}