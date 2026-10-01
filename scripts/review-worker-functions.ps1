. (Join-Path $PSScriptRoot 'review-card-instructions.ps1')

function Invoke-ReviewGit {
    param([string] $Root, [string[]] $Arguments)
    $startInfo = New-Object System.Diagnostics.ProcessStartInfo
    $startInfo.FileName = (Get-Command git -ErrorAction Stop).Source
    $startInfo.Arguments = (@('-C', $Root) + $Arguments | ForEach-Object { ConvertTo-ReviewProcessArgument $_ }) -join ' '
    $startInfo.UseShellExecute = $false
    $startInfo.RedirectStandardOutput = $true
    $startInfo.RedirectStandardError = $true
    $startInfo.StandardOutputEncoding = [System.Text.UTF8Encoding]::new($false)
    $startInfo.StandardErrorEncoding = [System.Text.UTF8Encoding]::new($false)
    $startInfo.CreateNoWindow = $true
    $process = New-Object System.Diagnostics.Process
    $process.StartInfo = $startInfo
    try {
        if (-not $process.Start()) { throw 'Could not start Git.' }
        # Drain both streams concurrently so a full diagnostic pipe cannot block Git.
        $stdoutTask = $process.StandardOutput.ReadToEndAsync()
        $stderrTask = $process.StandardError.ReadToEndAsync()
        $process.WaitForExit()
        $stdout = $stdoutTask.GetAwaiter().GetResult()
        $stderr = $stderrTask.GetAwaiter().GetResult()
        return [pscustomobject] @{
            ExitCode = $process.ExitCode
            StandardOutput = $stdout
            Output = (@($stdout, $stderr) | Where-Object { $_ } | ForEach-Object { $_.TrimEnd() }) -join "`n"
        }
    }
    finally { $process.Dispose() }
}

function Get-ReviewGitText {
    param([string] $Root, [string[]] $Arguments)
    $result = Invoke-ReviewGit $Root $Arguments
    if ($result.ExitCode -ne 0) { throw "Git $($Arguments -join ' ') failed: $($result.Output)" }
    # Successful machine-readable output must never include stderr warnings.
    return $result.StandardOutput.Trim()
}

function Get-ReviewChangedPaths {
    param([string] $Root)
    $tracked = Get-ReviewGitText $Root @('-c', 'core.quotepath=false', 'diff', '--name-only', 'HEAD')
    $untracked = Get-ReviewGitText $Root @('-c', 'core.quotepath=false', 'ls-files', '--others', '--exclude-standard')
    return @(("$tracked`n$untracked" -split "`n") | Where-Object { $_ } | Sort-Object -Unique)
}

function Test-ReviewDatabasePath {
    param([string] $Path)
    return $Path -match '^magical-vibes-review-server/review\.sqlite(?:-journal)?$'
}

function Assert-ReviewCheckout {
    param([string] $Root, [switch] $Pull)
    if ((Get-ReviewGitText $Root @('branch', '--show-current')) -ne 'main') { throw 'Review workers must stay on main.' }
    $dirty = @(Get-ReviewChangedPaths $Root | Where-Object { -not (Test-ReviewDatabasePath $_) })
    if ($dirty.Count -gt 0) { throw "The checkout has unrelated or unfinished changes: $($dirty -join ', '). Preserve or publish them before restarting this worker." }
    if ($Pull) { Get-ReviewGitText $Root @('pull', '--rebase', 'origin', 'main') | Out-Host }
}

function ConvertTo-ReviewProcessArgument {
    param([AllowEmptyString()][string] $Value)
    if ($Value.Length -gt 0 -and $Value -notmatch '[\s"]') { return $Value }
    $escaped = $Value -replace '(\\*)"', '$1$1\"'
    $escaped = $escaped -replace '(\\+)$', '$1$1'
    return '"' + $escaped + '"'
}

function Invoke-ReviewCodex {
    param([string] $Root, $Task, [string] $OutputPath, [string] $SchemaPath, [string] $LogPath)
    $instructions = Get-CardReviewInstructions
    $prompt = @"
/review-card $($Task.setCode) $($Task.collectorNumber)

$instructions

Review run $($Task.runId): $($Task.runName).
The shared implementation is $($Task.className), at $($Task.sourcePath). Review the full shared implementation and related card faces even if the context helper calls this printing a reprint. A registration-only check is insufficient.
Do not stage, commit, push, or switch branches. The worker publishes your permitted card-test changes. Production implementations, effects, predicates, docs, and test harness code are read-only.
Run the focused tests for any card test classes you change and use their results to inform your review. Never run the full test suite.
Your final response MUST follow the provided JSON schema. outcome is PASS when there are no real findings, otherwise FINDINGS. findings is an array of individual bug descriptions: explain what is wrong and why it matters, like the current text reports. Include no test code, test names, test output, patches, or coverage commentary in findings or the text report. Return the actual oracle card name as cardName. Do not report tool failures as card bugs: if the review cannot be completed, return ERROR with an empty findings array and an executionError description. For completed reviews, executionError must be null.
"@
    $arguments = @('--search', '--ask-for-approval', 'never', 'exec', '--ephemeral', '--json', '--model', [string] $Task.model,
        '--config', ('model_reasoning_effort="' + $Task.reasoningEffort + '"'), '--cd', $Root,
        '--output-schema', $SchemaPath, '--output-last-message', $OutputPath, '-')
    $startInfo = New-Object System.Diagnostics.ProcessStartInfo
    $startInfo.FileName = (Get-Command codex -ErrorAction Stop).Source
    $startInfo.Arguments = ($arguments | ForEach-Object { ConvertTo-ReviewProcessArgument $_ }) -join ' '
    $startInfo.WorkingDirectory = $Root
    $startInfo.UseShellExecute = $false
    $startInfo.RedirectStandardInput = $true
    $startInfo.RedirectStandardOutput = $true
    $startInfo.StandardOutputEncoding = [System.Text.UTF8Encoding]::new($false)
    $startInfo.CreateNoWindow = $true
    $process = New-Object System.Diagnostics.Process
    $process.StartInfo = $startInfo
    $writer = [System.IO.StreamWriter]::new($LogPath, $false, [System.Text.UTF8Encoding]::new($false))
    $started = $false
    $terminal = $false
    $completed = $false
    try {
        $started = $process.Start()
        if (-not $started) { throw 'Could not start Codex.' }
        $process.StandardInput.Write($prompt)
        $process.StandardInput.Close()
        while (($line = $process.StandardOutput.ReadLine()) -ne $null) {
            $writer.WriteLine($line)
            $writer.Flush()
            try { $event = $line | ConvertFrom-Json } catch { continue }
            if ($event.type -eq 'turn.completed' -or $event.type -eq 'turn.failed') {
                $terminal = $true
                $completed = $event.type -eq 'turn.completed'
                break
            }
        }
        if ($terminal) {
            if (-not $process.WaitForExit(10000)) { $process.Kill(); $process.WaitForExit() }
        } else { $process.WaitForExit() }
        if (-not $completed) { throw "Codex did not complete the review. Log: $LogPath" }
    }
    finally {
        if ($started -and -not $process.HasExited) { $process.Kill(); $process.WaitForExit() }
        $writer.Dispose()
        $process.Dispose()
    }
}

function Read-ReviewOutput {
    param([string] $Path)
    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) { throw 'Codex did not produce a structured review result.' }
    $review = Get-Content -LiteralPath $Path -Raw -Encoding UTF8 | ConvertFrom-Json
    if ($review.outcome -notin @('PASS', 'FINDINGS', 'ERROR') -or [string]::IsNullOrWhiteSpace($review.cardName)) { throw 'Invalid review outcome or card name.' }
    if ($null -eq $review.findings -or $review.findings -isnot [System.Array]) { throw 'The review findings must be an array.' }
    if (($review.outcome -ne 'FINDINGS' -and $review.findings.Count -ne 0) -or ($review.outcome -eq 'FINDINGS' -and $review.findings.Count -eq 0)) { throw 'The review verdict contradicts its findings.' }
    if ($review.outcome -eq 'ERROR' -and [string]::IsNullOrWhiteSpace($review.executionError)) { throw 'An unfinished review must include its execution failure.' }
    foreach ($finding in $review.findings) {
        if ($finding -isnot [string] -or [string]::IsNullOrWhiteSpace($finding)) { throw 'Each finding must be a non-empty bug description.' }
    }
    return $review
}

function Invoke-ReviewFocusedTests {
    param([string] $Root, [string[]] $Paths, [string] $LogDirectory)
    $failed = $false
    foreach ($path in $Paths) {
        $file = Join-Path $Root $path
        if (-not (Test-Path -LiteralPath $file)) { throw "Review deleted a test file: $path" }
        $source = [System.IO.File]::ReadAllText($file)
        $package = [regex]::Match($source, '\bpackage\s+([\w.]+)\s*;')
        if (-not $package.Success) { throw "Cannot resolve test package: $path" }
        $className = $package.Groups[1].Value + '.' + [System.IO.Path]::GetFileNameWithoutExtension($file)
        $startTime = Get-Date
        $savedPreference = $ErrorActionPreference
        Push-Location -LiteralPath $Root
        try {
            $ErrorActionPreference = 'Continue'
            & powershell.exe -NoProfile -File (Join-Path $Root 'scripts/run-card-test.ps1') $className *> (Join-Path $LogDirectory ([System.IO.Path]::GetFileNameWithoutExtension($file) + '.log'))
            $testExit = $LASTEXITCODE
        }
        finally { $ErrorActionPreference = $savedPreference; Pop-Location }
        if ($testExit -ne 0) {
            $xmlPath = Join-Path $Root "magical-vibes-application/build/test-results/test/TEST-$className.xml"
            if (-not (Test-Path -LiteralPath $xmlPath) -or (Get-Item -LiteralPath $xmlPath).LastWriteTime -lt $startTime) {
                throw "Focused tests did not run successfully (build or tooling failure): $className. Changes were preserved."
            }
            $suite = ([xml](Get-Content -LiteralPath $xmlPath -Raw -Encoding UTF8)).testsuite
            if ([int] $suite.failures + [int] $suite.errors -eq 0) { throw "Focused test execution failed without a behavioral test failure: $className" }
            $failed = $true
            Write-Host "Focused tests expose a failure in $className; publishing the review tests as requested."
        }
    }
    return $failed
}

function Publish-ReviewTests {
    param([string] $Root, $Task, [string[]] $Paths, [string] $ReviewedCommit, [string] $Directory)
    if ((Get-ReviewGitText $Root @('branch', '--show-current')) -ne 'main' -or (Get-ReviewGitText $Root @('rev-parse', 'HEAD')) -ne $ReviewedCommit) {
        throw 'Codex changed the branch or Git history; work was preserved.'
    }
    $unexpected = @(Get-ReviewChangedPaths $Root | Where-Object { -not (Test-ReviewDatabasePath $_) -and $_ -notin $Paths })
    if ($unexpected.Count -gt 0) { throw "Unexpected changes were preserved: $($unexpected -join ', ')" }
    if ($Paths.Count -eq 0) { return $null }
    Get-ReviewGitText $Root (@('add', '--') + $Paths) | Out-Null
    $messagePath = Join-Path $Directory 'commit-message.txt'
    $message = "Review $($Task.setCode) $($Task.collectorNumber) tests`n`nReview run $($Task.runId), model $($Task.model), reasoning $($Task.reasoningEffort).`n`nCo-authored-by: OpenAI Codex <codex@openai.com>`n"
    [System.IO.File]::WriteAllText($messagePath, $message, [System.Text.UTF8Encoding]::new($false))
    Get-ReviewGitText $Root (@('commit', '--only', '--file', $messagePath, '--') + $Paths) | Out-Host
    for ($attempt = 0; $attempt -lt 5; $attempt++) {
        $push = Invoke-ReviewGit $Root @('push', 'origin', 'main')
        if ($push.ExitCode -eq 0) { return Get-ReviewGitText $Root @('rev-parse', 'HEAD') }
        if ($attempt -eq 4) { throw "Could not push after five attempts. Commit was preserved. $($push.Output)" }
        Get-ReviewGitText $Root @('fetch', 'origin', 'main') | Out-Null
        $isBehind = Invoke-ReviewGit $Root @('merge-base', '--is-ancestor', 'origin/main', 'HEAD')
        if ($isBehind.ExitCode -eq 0) { throw "Push failed without a main race. Commit was preserved. $($push.Output)" }
        $rebase = Invoke-ReviewGit $Root @('rebase', 'origin/main')
        if ($rebase.ExitCode -ne 0) {
            Invoke-ReviewGit $Root @('rebase', '--abort') | Out-Null
            throw "Rebase conflict; the review commit was preserved for recovery. $($rebase.Output)"
        }
    }
}

function Invoke-ReviewTask {
    param([string] $Root, $Task, [string] $Directory)
    New-Item -ItemType Directory -Force -Path $Directory | Out-Null
    $reviewedCommit = Get-ReviewGitText $Root @('rev-parse', 'HEAD')
    $outputPath = Join-Path $Directory 'codex-result.json'
    $schemaPath = Join-Path $Directory 'result-schema.json'
    $schema = '{"type":"object","properties":{"outcome":{"type":"string","enum":["PASS","FINDINGS","ERROR"]},"cardName":{"type":"string"},"findings":{"type":"array","items":{"type":"string"}},"executionError":{"type":["string","null"]}},"required":["outcome","cardName","findings","executionError"],"additionalProperties":false}'
    [System.IO.File]::WriteAllText($schemaPath, $schema, [System.Text.UTF8Encoding]::new($false))
    $result = [ordered] @{ attemptToken = $Task.attemptToken; outcome = 'ERROR'; cardName = $null; findings = @(); reviewedCommit = $reviewedCommit; publicationStatus = 'NOT_REQUIRED'; publishedCommit = $null; executionError = $null; publicationError = $null }
    $stop = $false
    try {
        Invoke-ReviewCodex $Root $Task $outputPath $schemaPath (Join-Path $Directory 'codex.jsonl')
        $review = Read-ReviewOutput $outputPath
        if ($review.outcome -eq 'ERROR') { throw $review.executionError }
        $changes = @(Get-ReviewChangedPaths $Root | Where-Object { -not (Test-ReviewDatabasePath $_) })
        $unexpected = @($changes | Where-Object { $_ -notmatch '^magical-vibes-application/src/test/java/.+/cards/[^/]+/[^/]+\.java$' })
        if ($unexpected.Count -gt 0) { throw "Codex changed disallowed files: $($unexpected -join ', '). Changes were preserved." }
        $result.outcome = $review.outcome
        $result.cardName = $review.cardName
        $result.findings = @($review.findings)
        try {
            $testsFailed = Invoke-ReviewFocusedTests $Root $changes $Directory
            if ($testsFailed -and $review.outcome -eq 'PASS') { throw 'Focused tests failed although the review reported PASS. Test changes were preserved for recovery.' }
            $published = Publish-ReviewTests $Root $Task $changes $reviewedCommit $Directory
            if ($published) { $result.publicationStatus = 'PUSHED'; $result.publishedCommit = $published }
        }
        catch { $result.publicationStatus = 'FAILED'; $result.publicationError = $_.Exception.Message; $stop = $true }
    }
    catch {
        $result.executionError = $_.Exception.Message
        $dirty = @(Get-ReviewChangedPaths $Root | Where-Object { -not (Test-ReviewDatabasePath $_) })
        $stop = $dirty.Count -gt 0 -or (Get-ReviewGitText $Root @('branch', '--show-current')) -ne 'main' -or (Get-ReviewGitText $Root @('rev-parse', 'HEAD')) -ne $reviewedCommit
    }
    return [pscustomobject] @{ Result = $result; Stop = $stop }
}

function Send-ReviewPendingResult {
    param([string] $ServerUrl, [string] $PendingPath)
    $pending = Get-Content -LiteralPath $PendingPath -Raw -Encoding UTF8 | ConvertFrom-Json
    if ($pending.serverUrl -ne $ServerUrl.TrimEnd('/')) { throw "Pending result belongs to $($pending.serverUrl); restart using that server URL." }
    $body = $pending.result | ConvertTo-Json -Depth 12 -Compress
    Invoke-RestMethod -Uri "$($pending.serverUrl)/api/tasks/$($pending.taskId)/result" -Method Post -ContentType 'application/json; charset=utf-8' -Body ([System.Text.Encoding]::UTF8.GetBytes($body)) -TimeoutSec 30 | Out-Null
    Remove-Item -LiteralPath $PendingPath
    if ($pending.stop) { throw 'The result was saved to the server. Resolve the preserved checkout or publication failure before restarting.' }
}
