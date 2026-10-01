param(
    # Test class to run: either a bare card-test name ("ShockTest") or a
    # fully-qualified class name. Bare names resolve to
    # com.github.laxika.magicalvibes.cards.{letter}.{Name}.
    [Parameter(Mandatory = $true, Position = 0)]
    [ValidatePattern('^[A-Za-z_$][A-Za-z0-9_$]*(\.[A-Za-z_$][A-Za-z0-9_$]*)*$')]
    [string] $TestClass,

    # Includes Gradle startup, compilation and execution of this one class.
    [ValidateRange(1, 86400)][int] $TimeoutSeconds = 7200
)

$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot 'card-test-functions.ps1')
$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path

if ($TestClass -notmatch '\.') {
    $letter = $TestClass.Substring(0, 1).ToLowerInvariant()
    $TestClass = "com.github.laxika.magicalvibes.cards.$letter.$TestClass"
}

$buildDir = Join-Path $repositoryRoot 'magical-vibes-application/build'
if (-not (Test-Path $buildDir)) {
    New-Item -ItemType Directory -Force $buildDir | Out-Null
}
$log = Join-Path $buildDir "card-test.log"
$xmlPath = "$buildDir/test-results/test/TEST-$TestClass.xml"

function Test-XmlFresh {
    param([datetime] $Since)
    return (Test-Path $xmlPath) -and ((Get-Item $xmlPath).LastWriteTime -gt $Since)
}

$startTime = Get-Date
$exitCode = Invoke-CardTestGradle -Root $repositoryRoot -TestClass $TestClass -LogPath $log -TimeoutSeconds $TimeoutSeconds

if ($exitCode -eq 124) {
    Write-Host "Full log: $log"
    exit 124
}

if ($exitCode -eq 0) {
    $summary = ""
    if (Test-Path $xmlPath) {
        $suite = ([xml](Get-Content $xmlPath -Raw)).testsuite
        $seconds = [math]::Round([double]$suite.time, 1)
        $summary = " - $($suite.tests) tests, $($suite.skipped) skipped, ${seconds}s"
        if (-not (Test-XmlFresh -Since $startTime)) {
            $summary += " (up-to-date, not re-run)"
        }
    }
    Write-Host "PASS $TestClass$summary"
    exit 0
}

if (Test-XmlFresh -Since $startTime) {
    $suite = ([xml](Get-Content $xmlPath -Raw)).testsuite
    $cases = @($suite.testcase)
    $failed = @($cases | Where-Object { $_.failure -or $_.error })
    Write-Host "FAIL $TestClass - $($failed.Count) of $($cases.Count) tests failed"
    foreach ($case in $failed) {
        $node = $case.failure
        if (-not $node) { $node = $case.error }
        Write-Host ""
        Write-Host "- $($case.name)"
        $text = $node.'#text'
        if (-not $text) { $text = $node.message }
        # Keep the assertion message, "Caused by" lines, and project stack
        # frames; drop framework frames.
        $printed = 0
        foreach ($line in ($text -split "`r?`n")) {
            if ($line -match '^\s*at ' -and $line -notmatch 'magicalvibes') { continue }
            Write-Host "    $line"
            $printed++
            if ($printed -ge 25) {
                Write-Host "    ..."
                break
            }
        }
    }
} else {
    Write-Host "BUILD FAILED (exit $exitCode) - $TestClass did not run"
    $logLines = Get-Content $log
    $errorLines = @($logLines | Where-Object {
        $_ -match '(?i)\berror\b|FAILURE:|Caused by|No tests found'
    } | Select-Object -First 40)
    $errorLines | ForEach-Object { Write-Host "  $_" }
    Write-Host "  --- log tail ---"
    $logLines | Select-Object -Last 15 | ForEach-Object { Write-Host "  $_" }
}

Write-Host ""
Write-Host "Full log: $log"
exit $exitCode
