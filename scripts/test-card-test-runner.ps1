param()
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'card-test-functions.ps1')
$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$testParent = [System.IO.Path]::GetFullPath((Join-Path $repositoryRoot 'magical-vibes-review-server/build/card-test-runner-tests'))
$testRoot = Join-Path $testParent ([guid]::NewGuid().ToString('N') + ' with spaces')
New-Item -ItemType Directory -Force -Path $testRoot | Out-Null
$wrapper = Join-Path $testRoot 'gradlew.bat'
$log = Join-Path $testRoot 'focused test.log'
$testClass = 'example.cards.a.FirstCardTest'

function Assert-CardRunnerTest {
    param([bool] $Condition, [string] $Message)
    if (-not $Condition) { throw $Message }
}

try {
    [System.IO.File]::WriteAllText($wrapper, "@echo off`r`necho %*`r`necho compiler diagnostic 1>&2`r`nexit /b 0`r`n")
    $exitCode = Invoke-CardTestGradle $testRoot $testClass $log
    $output = [System.IO.File]::ReadAllText($log)
    Assert-CardRunnerTest ($exitCode -eq 0) 'The default two-hour build failed to launch from a path containing spaces'
    Assert-CardRunnerTest ($output -match ':magical-vibes-application:test --tests example\.cards\.a\.FirstCardTest --console=plain --no-daemon') 'The build did not use an exact single-class filter and a private daemon'
    Assert-CardRunnerTest ($output -match 'compiler diagnostic') 'Gradle stderr was not written to the log'
    Write-Host 'PASS exact class filter, paths containing spaces and combined build logs'

    foreach ($invalidClass in @('*', 'example.cards.*', 'FirstCardTest --tests *', 'example.CardTest&echo injected')) {
        $caught = $false
        try { Invoke-CardTestGradle $testRoot $invalidClass $log | Out-Null } catch { $caught = $true }
        Assert-CardRunnerTest $caught "Unsafe or broad test selector was accepted: $invalidClass"
    }
    Write-Host 'PASS wildcard, package and command-injection selectors are rejected'

    [System.IO.File]::WriteAllText($wrapper, "@echo off`r`nexit /b 7`r`n")
    $exitCode = Invoke-CardTestGradle $testRoot $testClass $log -TimeoutSeconds 5
    Assert-CardRunnerTest ($exitCode -eq 7) 'A Gradle failure exit code was lost'
    Write-Host 'PASS build failures retain the Gradle exit code'

    [System.IO.File]::WriteAllText((Join-Path $testRoot 'slow-compiler.bat'), "@echo off`r`nping -n 5 127.0.0.1 > nul`r`necho survived > survived.txt`r`n")
    [System.IO.File]::WriteAllText($wrapper, "@echo off`r`necho compiling many classes`r`ncmd /d /c slow-compiler.bat`r`n")
    $exitCode = Invoke-CardTestGradle $testRoot $testClass $log -TimeoutSeconds 1
    Assert-CardRunnerTest ($exitCode -eq 124) 'A compiler timeout was not reported as exit code 124'
    Start-Sleep -Seconds 5
    Assert-CardRunnerTest (-not (Test-Path -LiteralPath (Join-Path $testRoot 'survived.txt'))) 'A child compiler kept running after the timeout'
    Write-Host 'PASS timeout stops the build and its compiler process tree'
}
finally {
    $resolvedTestRoot = [System.IO.Path]::GetFullPath($testRoot)
    if (-not $resolvedTestRoot.StartsWith($testParent + [System.IO.Path]::DirectorySeparatorChar, [System.StringComparison]::OrdinalIgnoreCase)) { throw 'Refusing to remove a test directory outside the runner test workspace' }
    Remove-Item -LiteralPath $resolvedTestRoot -Recurse -Force
}
