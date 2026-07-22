param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]] $ServerArgs
)

$ErrorActionPreference = "Stop"

$node = Get-Command node -ErrorAction SilentlyContinue
if ($node) {
    $nodePath = $node.Source
} else {
    $candidates = @(
        (Join-Path $env:ProgramFiles "nodejs\node.exe"),
        (Join-Path ${env:ProgramFiles(x86)} "nodejs\node.exe"),
        (Join-Path $env:LOCALAPPDATA "Programs\nodejs\node.exe")
    ) | Where-Object { $_ -and (Test-Path -LiteralPath $_) }
    $nodePath = $candidates | Select-Object -First 1
}

if (-not $nodePath) {
    [Console]::Error.WriteLine("Node.js 20+ was not found on PATH or in a standard Windows install location.")
    exit 1
}

$serverPath = Join-Path $PSScriptRoot "server.mjs"
& $nodePath $serverPath @ServerArgs
exit $LASTEXITCODE
