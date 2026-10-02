# Examples:
#   .\scripts\run-build-and-fix.ps1
#   .\scripts\run-build-and-fix.ps1 -MaxFailuresPerBatch 25

param(
    [ValidateNotNullOrEmpty()][string] $Model = 'gpt-6.1-sol',
    [ValidateSet('low', 'medium', 'high', 'xhigh', 'max')][string] $Effort = 'high',
    [ValidateRange(1, 100)][int] $MaxFailuresPerBatch = 100
)

$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'build-repair-functions.ps1')
$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path

try {
    foreach ($command in @('git', 'codex')) {
        if (-not (Get-Command $command -ErrorAction SilentlyContinue)) { throw "The '$command' CLI was not found on PATH." }
    }
    Invoke-BuildRepairLoop -Root $repositoryRoot -Model $Model -Effort $Effort -MaxFailuresPerBatch $MaxFailuresPerBatch
}
catch {
    [Console]::Error.WriteLine("Build repair stopped: $($_.Exception.Message)")
    exit 1
}
exit 0
