. (Join-Path $PSScriptRoot 'review-card-instructions.ps1')

function Read-ReviewPricing {
    param([string] $Path = (Join-Path $PSScriptRoot 'review-model-pricing.json'))
    $configuration = Get-Content -LiteralPath $Path -Raw -Encoding UTF8 | ConvertFrom-Json
    if ($null -eq $configuration.models -or $configuration.models -isnot [pscustomobject]) { throw 'Review pricing must contain a models object.' }
    $prices = @{}
    foreach ($model in $configuration.models.PSObject.Properties) {
        $rates = @{}
        foreach ($field in @('inputUsdPerMillion', 'cachedInputUsdPerMillion', 'outputUsdPerMillion')) {
            $value = $model.Value.$field
            if ($null -eq $value -or $value -is [string] -or $value -is [bool] -or [decimal] $value -lt 0) { throw "Invalid $field rate for $($model.Name)." }
            $rates[$field] = [decimal] $value
        }
        $prices[$model.Name] = $rates
    }
    return $prices
}

function Get-ReviewUsage {
    param([string[]] $LogPaths, [string] $Model, [hashtable] $Pricing)
    $usage = [ordered] @{ inputTokens = $null; cachedInputTokens = $null; outputTokens = $null; estimatedCostUsd = $null }
    [long] $inputTokens = 0
    [long] $cachedTokens = 0
    [long] $outputTokens = 0
    foreach ($path in $LogPaths) {
        # Missing usage for any invocation makes the total unknown, rather than zero or a partial estimate.
        if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { return $usage }
        $found = $false
        $reader = [System.IO.StreamReader]::new($path, [System.Text.Encoding]::UTF8)
        try {
            while (($line = $reader.ReadLine()) -ne $null) {
                try { $event = $line | ConvertFrom-Json } catch { continue }
                if ($event.type -notin @('turn.completed', 'turn.failed')) { continue }
                foreach ($field in @('input_tokens', 'cached_input_tokens', 'output_tokens')) {
                    $value = $event.usage.$field
                    if ($null -eq $value -or $value -isnot [ValueType] -or $value -is [bool] -or $value -lt 0 -or [decimal] $value -ne [decimal] [long] $value) { return $usage }
                }
                if ($event.usage.cached_input_tokens -gt $event.usage.input_tokens) { return $usage }
                $inputTokens += [long] $event.usage.input_tokens
                $cachedTokens += [long] $event.usage.cached_input_tokens
                $outputTokens += [long] $event.usage.output_tokens
                $found = $true
            }
        }
        finally { $reader.Dispose() }
        if (-not $found) { return $usage }
    }
    $usage.inputTokens = $inputTokens
    $usage.cachedInputTokens = $cachedTokens
    $usage.outputTokens = $outputTokens
    if ($Pricing -and $Pricing.ContainsKey($Model)) {
        $rates = $Pricing[$Model]
        $usage.estimatedCostUsd = (([decimal] $inputTokens - $cachedTokens) * $rates.inputUsdPerMillion +
            [decimal] $cachedTokens * $rates.cachedInputUsdPerMillion + [decimal] $outputTokens * $rates.outputUsdPerMillion) / 1000000
    }
    return $usage
}

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

function Get-ReviewCodexPrompt {
    param($Task)
    $instructions = Get-CardReviewInstructions
    return @"
/review-card $($Task.setCode) $($Task.collectorNumber)

$instructions

Review run $($Task.runId): $($Task.runName).
The shared implementation is $($Task.className), at $($Task.sourcePath). Review the full shared implementation and related card faces even if the context helper calls this printing a reprint. A registration-only check is insufficient.
Do not stage, commit, push, or switch branches. The worker publishes your permitted card-test changes. Production implementations, effects, predicates, docs, and test harness code are read-only.
Do not launch Gradle or run tests yourself in this worker session. Finish the oracle/implementation review and create or update the needed card tests, then return your structured review result. Only edit test classes belonging to the card under review (including its related faces). The worker publishes permitted test changes directly without local compilation or test execution; the CI server validates them and alerts us to failures. Tests that have not been run are not an executionError; do not return ERROR because validation is deferred to CI.
Only create or edit card test files under magical-vibes-application/src/test/java/.../cards/{letter}/. Effect tests are read-only, even when the effect is named after the reviewed card: for Pacifism, edit PacifismTest.java, never PacifismEffectTest.java. This restriction also applies to annotation maintenance, incorrect-test fixes, unused-code cleanup, helper refactoring, and coverage additions. Put new behavioral coverage in the card's own test class; report any effect-test issues without editing those files. Editing effect tests causes the worker to reject the review for changing disallowed files.
Your final response MUST follow the provided JSON schema. outcome is PASS when there are no real findings, otherwise FINDINGS. findings is an array of individual bug descriptions: explain what is wrong and why it matters, like the current text reports. Include no test code, test names, test output, patches, or coverage commentary in findings or the text report. Return the actual oracle card name as cardName. Do not report tool failures as card bugs: if the review cannot be completed, return ERROR with an empty findings array and an executionError description. For completed reviews, executionError must be null.
Permitted test changes are published for both PASS and FINDINGS reviews. PASS describes the card review verdict; it does not claim that compilation or tests succeeded. CI failures are handled separately from the review verdict.
"@
}

function Invoke-ReviewCodex {
    param([string] $Root, $Task, [string] $OutputPath, [string] $SchemaPath, [string] $LogPath)
    $prompt = Get-ReviewCodexPrompt $Task
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
    $maximumAttempts = 30
    for ($attempt = 0; $attempt -lt $maximumAttempts; $attempt++) {
        Get-ReviewGitText $Root @('fetch', 'origin', 'main') | Out-Null
        $isBehind = Invoke-ReviewGit $Root @('merge-base', '--is-ancestor', 'origin/main', 'HEAD')
        if ($isBehind.ExitCode -notin @(0, 1)) { throw "Could not compare main histories. Commit was preserved. $($isBehind.Output)" }
        if ($isBehind.ExitCode -eq 1) {
            $rebase = Invoke-ReviewGit $Root @('rebase', 'origin/main')
            if ($rebase.ExitCode -ne 0) {
                Invoke-ReviewGit $Root @('rebase', '--abort') | Out-Null
                throw "Rebase conflict; the review commit was preserved for recovery. $($rebase.Output)"
            }
        }
        $push = Invoke-ReviewGit $Root @('push', 'origin', 'main')
        if ($push.ExitCode -eq 0) { return Get-ReviewGitText $Root @('rev-parse', 'HEAD') }
        if ($attempt -eq ($maximumAttempts - 1)) { throw "Could not push after $maximumAttempts attempts. Commit was preserved. $($push.Output)" }
        Get-ReviewGitText $Root @('fetch', 'origin', 'main') | Out-Null
        $isBehind = Invoke-ReviewGit $Root @('merge-base', '--is-ancestor', 'origin/main', 'HEAD')
        if ($isBehind.ExitCode -notin @(0, 1)) { throw "Could not compare main histories after a failed push. Commit was preserved. $($isBehind.Output)" }
        if ($isBehind.ExitCode -eq 0) { throw "Push failed without a main race. Commit was preserved. $($push.Output)" }
        $delayMilliseconds = 500 * [Math]::Min($attempt + 1, 10) + (Get-Random -Minimum 0 -Maximum 1001)
        Write-Warning "Remote main advanced during publication. Retrying push ($($attempt + 2)/$maximumAttempts) in $delayMilliseconds ms."
        Start-Sleep -Milliseconds $delayMilliseconds
    }
}

function Invoke-ReviewTask {
    param([string] $Root, $Task, [string] $Directory, [hashtable] $Pricing = (Read-ReviewPricing))
    New-Item -ItemType Directory -Force -Path $Directory | Out-Null
    $reviewedCommit = Get-ReviewGitText $Root @('rev-parse', 'HEAD')
    $outputPath = Join-Path $Directory 'codex-result.json'
    $schemaPath = Join-Path $Directory 'result-schema.json'
    $schema = '{"type":"object","properties":{"outcome":{"type":"string","enum":["PASS","FINDINGS","ERROR"]},"cardName":{"type":"string"},"findings":{"type":"array","items":{"type":"string"}},"executionError":{"type":["string","null"]}},"required":["outcome","cardName","findings","executionError"],"additionalProperties":false}'
    [System.IO.File]::WriteAllText($schemaPath, $schema, [System.Text.UTF8Encoding]::new($false))
    $result = [ordered] @{ attemptToken = $Task.attemptToken; outcome = 'ERROR'; cardName = $null; findings = @(); reviewedCommit = $reviewedCommit; publicationStatus = 'NOT_REQUIRED'; publishedCommit = $null; executionError = $null; publicationError = $null }
    $stop = $false
    $logPaths = @((Join-Path $Directory 'codex.jsonl'))
    try {
        Invoke-ReviewCodex $Root $Task $outputPath $schemaPath $logPaths[0]
        $review = Read-ReviewOutput $outputPath
        if ($review.outcome -eq 'ERROR') { throw $review.executionError }
        $changes = @(Get-ReviewChangedPaths $Root | Where-Object { -not (Test-ReviewDatabasePath $_) })
        $unexpected = @($changes | Where-Object { $_ -notmatch '^magical-vibes-application/src/test/java/.+/cards/[^/]+/[^/]+\.java$' })
        if ($unexpected.Count -gt 0) { throw "Codex changed disallowed files: $($unexpected -join ', '). Changes were preserved." }
        $result.outcome = $review.outcome
        $result.cardName = $review.cardName
        $result.findings = @($review.findings)
        try {
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
    $usage = [ordered] @{ inputTokens = $null; cachedInputTokens = $null; outputTokens = $null; estimatedCostUsd = $null }
    try { $usage = Get-ReviewUsage $logPaths $Task.model $Pricing }
    catch { Write-Warning "Could not read review usage: $($_.Exception.Message)" }
    foreach ($field in $usage.Keys) { $result[$field] = $usage[$field] }
    return [pscustomobject] @{ Result = $result; Stop = $stop }
}

function Send-ReviewPendingResult {
    param([string] $ServerUrl, [string] $PendingPath)
    $pending = Get-Content -LiteralPath $PendingPath -Raw -Encoding UTF8 | ConvertFrom-Json
    if ($pending.serverUrl -ne $ServerUrl.TrimEnd('/')) { throw "Pending result belongs to $($pending.serverUrl); restart using that server URL." }
    $body = $pending.result | ConvertTo-Json -Depth 12 -Compress
    Invoke-RestMethod -Uri "$($pending.serverUrl)/api/tasks/$($pending.taskId)/result" -Method Post -ContentType 'application/json; charset=utf-8' -Body ([System.Text.Encoding]::UTF8.GetBytes($body)) -TimeoutSec 30 | Out-Null
    Remove-Item -LiteralPath $PendingPath
    if ($pending.stop) {
        $reason = @($pending.result.executionError, $pending.result.publicationError) | Where-Object { $_ }
        throw "The result was saved to the server. Worker stopped with changes preserved: $($reason -join '; '). Resolve the checkout or publication failure before restarting."
    }
}
