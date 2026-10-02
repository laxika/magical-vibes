function Invoke-CardTestGradle {
    param(
        [Parameter(Mandatory = $true)]
        [string] $Root,
        [Parameter(Mandatory = $true)]
        [ValidatePattern('^[A-Za-z_$][A-Za-z0-9_$]*(\.[A-Za-z_$][A-Za-z0-9_$]*)+$')]
        [string] $TestClass,
        [Parameter(Mandatory = $true)]
        [string] $LogPath,
        [ValidateRange(1, 86400)][int] $TimeoutSeconds = 7200
    )

    $startInfo = New-Object System.Diagnostics.ProcessStartInfo
    $startInfo.FileName = $env:ComSpec
    # A private Gradle daemon lets a timeout stop this build's process tree
    # without stopping another terminal's Gradle build.
    $wrapper = Join-Path $Root 'gradlew.bat'
    $startInfo.Arguments = '/d /s /c ""' + $wrapper + '" :magical-vibes-application:test --tests ' + $TestClass + ' --console=plain --no-daemon > "' + $LogPath + '" 2>&1"'
    $startInfo.WorkingDirectory = $Root
    $startInfo.UseShellExecute = $false
    $startInfo.CreateNoWindow = $true
    $process = New-Object System.Diagnostics.Process
    $process.StartInfo = $startInfo
    $started = $false
    $elapsed = [System.Diagnostics.Stopwatch]::StartNew()
    try {
        $started = $process.Start()
        if (-not $started) { throw 'Could not start the focused Gradle build.' }
        while ($true) {
            $remainingMilliseconds = [int][math]::Ceiling(($TimeoutSeconds - $elapsed.Elapsed.TotalSeconds) * 1000)
            if ($remainingMilliseconds -le 0) {
                Write-Host "TIMEOUT after $TimeoutSeconds seconds - $TestClass compilation/test execution did not finish. Log: $LogPath"
                return 124
            }
            if ($process.WaitForExit([math]::Min(60000, $remainingMilliseconds))) { return $process.ExitCode }
            Write-Host "Still waiting for $TestClass compilation/tests ($([int] $elapsed.Elapsed.TotalMinutes) minutes elapsed; limit $TimeoutSeconds seconds). Log: $LogPath"
        }
    }
    finally {
        if ($started -and -not $process.HasExited) {
            $savedPreference = $ErrorActionPreference
            try {
                $ErrorActionPreference = 'Continue'
                & taskkill.exe /PID $process.Id /T /F *> $null
            }
            finally { $ErrorActionPreference = $savedPreference }
            if (-not $process.WaitForExit(10000)) { $process.Kill(); $process.WaitForExit() }
        }
        $elapsed.Stop()
        $process.Dispose()
    }
}
