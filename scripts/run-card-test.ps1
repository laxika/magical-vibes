param(
    # Test class to run: either a bare card-test name ("ShockTest") or a
    # fully-qualified class name. Bare names resolve to
    # com.github.laxika.magicalvibes.cards.{letter}.{Name}.
    [Parameter(Mandatory = $true, Position = 0)]
    [string] $TestClass
)

$ErrorActionPreference = "Stop"

if ($TestClass -notmatch '\.') {
    $letter = $TestClass.Substring(0, 1).ToLowerInvariant()
    $TestClass = "com.github.laxika.magicalvibes.cards.$letter.$TestClass"
}

$buildDir = "magical-vibes-application/build"
if (-not (Test-Path $buildDir)) {
    New-Item -ItemType Directory -Force $buildDir | Out-Null
}
$log = Join-Path $buildDir "card-test.log"
$xmlPath = "$buildDir/test-results/test/TEST-$TestClass.xml"

function Invoke-GradleTest {
    param([bool] $SkipFrontend)

    $gradleArgs = ":magical-vibes-application:test --tests $TestClass --console=plain"
    if ($SkipFrontend) {
        $gradleArgs += " -x :magical-vibes-frontend:buildAngular"
    }
    # Redirect inside cmd so PowerShell 5.1 does not wrap stderr lines in
    # NativeCommandError records.
    & cmd /c ".\gradlew.bat $gradleArgs > `"$log`" 2>&1"
    return $LASTEXITCODE
}

function Test-XmlFresh {
    param([datetime] $Since)
    return (Test-Path $xmlPath) -and ((Get-Item $xmlPath).LastWriteTime -gt $Since)
}

$startTime = Get-Date
$exitCode = Invoke-GradleTest -SkipFrontend $true

# A build failure (no fresh test results) whose FAILURE section mentions the
# frontend usually means stale frontend assets; retry once without skipping
# buildAngular. Checking only the FAILURE section avoids false positives from
# the "> Task :magical-vibes-frontend:..." lines every build prints.
function Test-FrontendFailure {
    $lines = Get-Content $log
    $failureStart = -1
    for ($i = 0; $i -lt $lines.Count; $i++) {
        if ($lines[$i] -match '^FAILURE:') {
            $failureStart = $i
            break
        }
    }
    if ($failureStart -lt 0) { return $false }
    return [bool]($lines[$failureStart..($lines.Count - 1)] -match 'frontend')
}

if ($exitCode -ne 0 -and -not (Test-XmlFresh -Since $startTime) -and (Test-FrontendFailure)) {
    Write-Host "(build failed mentioning frontend - retrying without -x :magical-vibes-frontend:buildAngular)"
    $exitCode = Invoke-GradleTest -SkipFrontend $false
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
