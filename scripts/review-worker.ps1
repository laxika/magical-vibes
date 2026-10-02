<#
.SYNOPSIS
    Claims review tasks from the dashboard's active run and publishes card-test changes to main.
.EXAMPLE
    .\scripts\review-worker.ps1 -ServerUrl http://localhost:8091
#>
param(
    [string] $ServerUrl = 'http://localhost:8091',
    [string] $WorkerId,
    [string] $PricingPath = (Join-Path $PSScriptRoot 'review-model-pricing.json'),
    [ValidateRange(1, 3600)][int] $PollSeconds = 5
)

$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'review-worker-functions.ps1')
$pricing = Read-ReviewPricing $PricingPath
$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
if (-not $WorkerId) { $WorkerId = Split-Path -Leaf $repositoryRoot }
$ServerUrl = $ServerUrl.TrimEnd('/')
$serverUri = [uri] $ServerUrl
if (-not $serverUri.IsAbsoluteUri -or $serverUri.Scheme -notin @('http', 'https')) { throw 'ServerUrl must be an absolute HTTP URL.' }
foreach ($command in @('git', 'codex')) { Get-Command $command -ErrorAction Stop | Out-Null }
$workerDirectory = Join-Path $repositoryRoot 'magical-vibes-review-server/build/worker'
New-Item -ItemType Directory -Force -Path $workerDirectory | Out-Null
$pendingPath = Join-Path $workerDirectory 'pending-result.json'
$workerLock = [System.IO.File]::Open((Join-Path $workerDirectory 'worker.lock'), [System.IO.FileMode]::OpenOrCreate, [System.IO.FileAccess]::ReadWrite, [System.IO.FileShare]::None)
Write-Host "Review worker $WorkerId connected to $ServerUrl. Stop with Ctrl+C."

try {
while ($true) {
    if (Test-Path -LiteralPath $pendingPath) {
        try { Send-ReviewPendingResult $ServerUrl $pendingPath }
        catch {
            if (-not (Test-Path -LiteralPath $pendingPath)) { throw }
            if ($_.Exception.Response -and [int] $_.Exception.Response.StatusCode -ge 400 -and [int] $_.Exception.Response.StatusCode -lt 500 -and [int] $_.Exception.Response.StatusCode -ne 429) {
                Move-Item -LiteralPath $pendingPath -Destination (Join-Path $workerDirectory ('rejected-result-' + [guid]::NewGuid().ToString('N') + '.json'))
                throw 'The server rejected this result. It was preserved in the worker directory; inspect the task before restarting.'
            }
            Write-Warning "Could not upload the saved result: $($_.Exception.Message). Retrying upload without reviewing again."
            Start-Sleep -Seconds $PollSeconds
            continue
        }
    }
    Assert-ReviewCheckout $repositoryRoot
    try {
        $overview = Invoke-RestMethod -Uri "$ServerUrl/api/overview" -TimeoutSec 30
        $active = @($overview.runs | Where-Object { $_.active -and $_.created -gt 0 })
        if ($active.Count -eq 0) { Start-Sleep -Seconds $PollSeconds; continue }
    }
    catch { Write-Warning "Review server unavailable: $($_.Exception.Message)"; Start-Sleep -Seconds $PollSeconds; continue }
    Assert-ReviewCheckout $repositoryRoot -Pull
    $claimBody = @{ workerId = $WorkerId } | ConvertTo-Json -Compress
    $task = Invoke-RestMethod -Uri "$ServerUrl/api/tasks/claim" -Method Post -ContentType 'application/json; charset=utf-8' -Body ([System.Text.Encoding]::UTF8.GetBytes($claimBody)) -TimeoutSec 30
    if (-not $task) { Start-Sleep -Seconds $PollSeconds; continue }
    Write-Host "Run $($task.runId): $($task.setCode) $($task.collectorNumber), $($task.model) / $($task.reasoningEffort)"
    $directory = Join-Path $workerDirectory ([string] $task.attemptToken)
    $completed = Invoke-ReviewTask $repositoryRoot $task $directory $pricing
    $pending = @{ serverUrl = $ServerUrl; taskId = $task.id; result = $completed.Result; stop = $completed.Stop }
    $pendingJson = $pending | ConvertTo-Json -Depth 12
    $temporaryPath = $pendingPath + '.tmp'
    [System.IO.File]::WriteAllText($temporaryPath, $pendingJson, [System.Text.UTF8Encoding]::new($false))
    Move-Item -LiteralPath $temporaryPath -Destination $pendingPath
    $cost = 'unavailable (usage or model pricing missing)'
    if ($null -ne $completed.Result.estimatedCostUsd) { $cost = '$' + $completed.Result.estimatedCostUsd.ToString('F6', [System.Globalization.CultureInfo]::InvariantCulture) + ' USD' }
    Write-Host "$($completed.Result.outcome): $($completed.Result.findings.Count) findings. Estimated token cost: $cost. Result saved for upload."
}
}
finally { $workerLock.Dispose() }
