# Examples:
#   .\scripts\run-fuzz-and-fix.ps1
#   .\scripts\run-fuzz-and-fix.ps1 -Rounds 500 -Effort high
#   .\scripts\run-fuzz-and-fix.ps1 -Agent codex -Model gpt-5.6-luna -Effort xhigh

param(
    # Number of random AI games to run. The repository default for this script is 500.
    [ValidateRange(1, 1000000)]
    [int] $Rounds = 5000,

    # Coding agent CLI used to diagnose and fix a failed fuzz run.
    [ValidateSet("claude", "codex")]
    [string] $Agent = "claude",

    # Model used by the repair instance. Defaults to claude-opus-5-5 for Claude and gpt-6.1-sol for Codex.
    [string] $Model,

    # Reasoning effort used by the repair instance. Defaults to medium for Claude and high for Codex.
    [ValidateSet("low", "medium", "high", "xhigh", "max")]
    [string] $Effort
)

$ErrorActionPreference = "Stop"

if ($Agent -eq "claude") {
    $agentName = "Claude"
    if (-not $Model) { $Model = "claude-opus-5-5" }
    if (-not $Effort) { $Effort = "medium" }
    if ($Model -eq "claude-opus-5-5") {
        $commitCoAuthor = "Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>"
    }
    else {
        $commitCoAuthor = "Co-Authored-By: Claude <noreply@anthropic.com>"
    }
}
else {
    $agentName = "Codex"
    if (-not $Model) { $Model = "gpt-6.1-sol" }
    if (-not $Effort) { $Effort = "high" }
    $commitCoAuthor = "Co-authored-by: OpenAI Codex <codex@openai.com>"
}

$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$gradleWrapper = Join-Path $repositoryRoot "gradlew.bat"
$logPath = Join-Path $repositoryRoot "fuzz.log"
$testClass = "com.github.laxika.magicalvibes.ai.RandomAiFuzzTest"

function ConvertTo-NativeProcessArgument {
    param(
        [AllowEmptyString()]
        [string] $Value
    )

    if ($Value.Length -gt 0 -and $Value -notmatch '[\s"]') {
        return $Value
    }

    $escaped = $Value -replace '(\\*)"', '$1$1\"'
    $escaped = $escaped -replace '(\\+)$', '$1$1'
    return '"' + $escaped + '"'
}

if (-not (Test-Path -LiteralPath $gradleWrapper)) {
    Write-Error "Gradle wrapper not found at $gradleWrapper."
    exit 1
}

if (-not (Get-Command $Agent -ErrorAction SilentlyContinue)) {
    Write-Error "The '$Agent' CLI was not found on PATH."
    exit 1
}

if (-not (Get-Command git -ErrorAction SilentlyContinue)) {
    Write-Error "The 'git' CLI was not found on PATH."
    exit 1
}

$branch = (& git -C $repositoryRoot branch --show-current).Trim()
if ($LASTEXITCODE -ne 0) {
    Write-Error "Could not determine the current Git branch."
    exit 1
}
if ($branch -ne "main") {
    Write-Error "Fuzz repairs must run on main; the current branch is '$branch'."
    exit 1
}

$initialStatus = @(& git -C $repositoryRoot status --porcelain --untracked-files=all)
if ($LASTEXITCODE -ne 0) {
    Write-Error "Could not inspect the Git working tree."
    exit 1
}
if ($initialStatus.Count -gt 0) {
    Write-Error "The working tree must be clean before an automated fuzz repair can run."
    exit 1
}

$startingHead = (& git -C $repositoryRoot rev-parse HEAD).Trim()
if ($LASTEXITCODE -ne 0) {
    Write-Error "Could not determine the starting Git commit."
    exit 1
}

while ($true) {
Write-Host "Running $Rounds Random AI fuzz games. Full output: $logPath"

Push-Location $repositoryRoot
try {
    $gradleCommand = ".\gradlew.bat :magical-vibes-ai:test --tests `"$testClass`" -DrunCardFuzz=true -DfuzzGames=$Rounds -Dorg.gradle.jvmargs=`"-Xmx6g`" --info --rerun --console=plain > `"$logPath`" 2>&1"
    & cmd /d /s /c $gradleCommand
    $gradleExitCode = $LASTEXITCODE
}
finally {
    Pop-Location
}

if (-not (Test-Path -LiteralPath $logPath)) {
    Write-Error "The fuzz command produced no log at $logPath."
    exit 1
}

$successMarker = "All $Rounds fuzz games passed."
$logShowsSuccess = [bool](Select-String -LiteralPath $logPath -SimpleMatch $successMarker -Quiet)
$logShowsFailure = [bool](Select-String -LiteralPath $logPath -Pattern '^FAILURE:|^BUILD FAILED|^RandomAiFuzzTest > .* FAILED$|^> Task :magical-vibes-ai:test FAILED$' -Quiet)

if ($gradleExitCode -eq 0 -and $logShowsSuccess -and -not $logShowsFailure) {
    Write-Host "PASS: all $Rounds fuzz games completed successfully."
    exit 0
}

Write-Warning "The fuzz run failed or did not reach its success marker (Gradle exit code $gradleExitCode)."
$failedGameIndex = $null
$gameIndexPattern = '^=== Game #(?<index>[0-9]+)/[0-9]+ ===$'
Select-String -LiteralPath $logPath -Pattern $gameIndexPattern | ForEach-Object {
    $failedGameIndex = $_.Matches[-1].Groups['index'].Value
}
if ($failedGameIndex) {
    Write-Warning "Game #$failedGameIndex failed."
}
else {
    Write-Warning "The failed game index could not be determined from $logPath."
}
Write-Host "Relevant log lines:"
Select-String -LiteralPath $logPath -Pattern '^FAILURE:|^BUILD FAILED|^RandomAiFuzzTest > .* FAILED$|^> Task :magical-vibes-ai:test FAILED$|^All [0-9]+ fuzz games passed\.$' |
    Select-Object -Last 20 |
    ForEach-Object { Write-Host "  $($_.Line)" }
Write-Host "Starting a fresh $agentName repair instance (model: $Model, effort: $Effort)..."

$commitOutputPath = Join-Path ([System.IO.Path]::GetTempPath()) ("magical-vibes-fuzz-commit-{0}.json" -f [Guid]::NewGuid().ToString("N"))
$commitSchemaPath = Join-Path ([System.IO.Path]::GetTempPath()) ("magical-vibes-fuzz-schema-{0}.json" -f [Guid]::NewGuid().ToString("N"))
$commitMessagePath = Join-Path ([System.IO.Path]::GetTempPath()) ("magical-vibes-fuzz-message-{0}.txt" -f [Guid]::NewGuid().ToString("N"))
$commitSchema = @"
{
  "type": "object",
  "properties": {
    "subject": {
      "type": "string",
      "description": "An imperative, descriptive Git commit subject of at most 72 characters.",
      "maxLength": 72,
      "pattern": "^[^\\r\\n]+$"
    },
    "body": {
      "type": "string",
      "description": "One to three concise sentences explaining the root cause, fix, and regression coverage."
    }
  },
  "required": ["subject", "body"],
  "additionalProperties": false
}
"@

$prompt = @"
The RandomAiFuzzTest run for $Rounds games failed with Gradle exit code $gradleExitCode.

Inspect the complete fuzz log at fuzz.log and any test results generated by this run under magical-vibes-ai/build/test-results/test (verify result timestamps before relying on them). Diagnose the root cause and implement a rules-correct fix. Follow AGENTS.md and preserve unrelated working-tree changes. Do not stage, commit, push, or switch branches; this script will commit and push after you finish. Add focused behavioral regression tests where practical. If the fix changes AiDecisionEngine.java, always add or update focused integration coverage in EasyAiDecisionEngineTest, MediumAiDecisionEngineTest, and HardAiDecisionEngineTest; do not rely only on AiDecisionEngineTest. Do not run the full test suite or rerun the 500-game fuzz test; run only focused tests relevant to the fix. If Magic rules are ambiguous, verify the official ruling before changing behavior. Do not ask clarifying questions or stop at a diagnosis: make the best fix you can and verify it.

In your final response, provide a concise imperative commit subject and a descriptive body based on the actual root cause, fix, and tests. Never spell out a card set's full name; use only its short set code. Do not include a Co-authored-by trailer because the script adds it.
"@

try {
    $utf8WithoutBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($commitSchemaPath, $commitSchema, $utf8WithoutBom)

    if ($Agent -eq "claude") {
        # Claude takes the schema inline and reports the structured output on its
        # final result event, which is written to $commitOutputPath below.
        $compactCommitSchema = $commitSchema | ConvertFrom-Json | ConvertTo-Json -Compress -Depth 10
        $agentArguments = @(
            "--print",
            "--model", $Model,
            "--effort", $Effort,
            "--dangerously-skip-permissions",
            "--output-format", "stream-json",
            "--verbose",
            "--json-schema", $compactCommitSchema
        )
    }
    else {
        $agentArguments = @(
            "--search",
            "--ask-for-approval", "never",
            "exec",
            "--ephemeral",
            "--json",
            "--model", $Model,
            "--config", "model_reasoning_effort=`"$Effort`"",
            "--cd", $repositoryRoot,
            "--output-schema", $commitSchemaPath,
            "--output-last-message", $commitOutputPath,
            "-"
        )
    }
    # Drive automation from the agent's terminal JSON event instead of waiting for
    # every descendant to release the native stdout pipe after a long tool run.
    $agentStartInfo = New-Object System.Diagnostics.ProcessStartInfo
    $agentStartInfo.FileName = (Get-Command $Agent).Source
    $agentStartInfo.Arguments = ($agentArguments | ForEach-Object { ConvertTo-NativeProcessArgument $_ }) -join " "
    $agentStartInfo.WorkingDirectory = $repositoryRoot
    $agentStartInfo.UseShellExecute = $false
    $agentStartInfo.RedirectStandardInput = $true
    $agentStartInfo.RedirectStandardOutput = $true
    $agentStartInfo.RedirectStandardError = $false
    $agentStartInfo.CreateNoWindow = $false
    $agentStartInfo.StandardOutputEncoding = $utf8WithoutBom

    $agentProcess = New-Object System.Diagnostics.Process
    $agentProcess.StartInfo = $agentStartInfo
    $agentProcessStarted = $false
    $agentTurnCompleted = $false
    $agentTurnFailed = $false
    try {
        if (-not $agentProcess.Start()) {
            Write-Error "Could not start $agentName."
            exit 1
        }
        $agentProcessStarted = $true

        $agentProcess.StandardInput.Write($prompt)
        $agentProcess.StandardInput.Close()

        while (($agentLine = $agentProcess.StandardOutput.ReadLine()) -ne $null) {
            try {
                $agentEvent = $agentLine | ConvertFrom-Json
            }
            catch {
                Write-Host $agentLine
                continue
            }

            if ($Agent -eq "claude") {
                switch ($agentEvent.type) {
                    "system" {
                        if ($agentEvent.subtype -eq "init") {
                            Write-Host "Claude repair session started: $($agentEvent.session_id)"
                        }
                    }
                    "assistant" {
                        foreach ($content in @($agentEvent.message.content)) {
                            if ($content.type -eq "text" -and $content.text) {
                                Write-Host $content.text
                            }
                            elseif ($content.type -eq "tool_use") {
                                if ($content.name -eq "Bash" -and $content.input.command) {
                                    Write-Host "Claude command: $($content.input.command)"
                                }
                                elseif ($content.input.file_path) {
                                    Write-Host "Claude $($content.name): $($content.input.file_path)"
                                }
                                elseif ($content.name -ne "StructuredOutput") {
                                    Write-Host "Claude tool: $($content.name)"
                                }
                            }
                        }
                    }
                    "result" {
                        $usage = $agentEvent.usage
                        $usageSummary = "input: $($usage.input_tokens), cached: $($usage.cache_read_input_tokens), output: $($usage.output_tokens), cost: `$$($agentEvent.total_cost_usd)"
                        if ($agentEvent.subtype -eq "success" -and -not $agentEvent.is_error -and $agentEvent.structured_output) {
                            $agentTurnCompleted = $true
                            $commitMetadataJson = $agentEvent.structured_output | ConvertTo-Json -Compress -Depth 10
                            [System.IO.File]::WriteAllText($commitOutputPath, $commitMetadataJson, $utf8WithoutBom)
                            Write-Host "Claude repair session completed ($usageSummary)."
                        }
                        else {
                            $agentTurnFailed = $true
                            Write-Warning "Claude repair session failed ($($agentEvent.subtype); $usageSummary): $($agentEvent.result)"
                        }
                    }
                }
            }
            else {
                switch ($agentEvent.type) {
                    "thread.started" {
                        Write-Host "Codex repair thread started: $($agentEvent.thread_id)"
                    }
                    "item.completed" {
                        if ($agentEvent.item.type -eq "agent_message") {
                            Write-Host $agentEvent.item.text
                        }
                        elseif ($agentEvent.item.type -eq "command_execution") {
                            Write-Host "Codex command $($agentEvent.item.status): $($agentEvent.item.command)"
                        }
                    }
                    "error" {
                        Write-Warning "Codex reported an error: $($agentEvent.message)"
                    }
                    "turn.failed" {
                        $agentTurnFailed = $true
                        Write-Warning "Codex repair turn failed."
                    }
                    "turn.completed" {
                        $agentTurnCompleted = $true
                        $usage = $agentEvent.usage
                        Write-Host "Codex repair turn completed (input: $($usage.input_tokens), cached: $($usage.cached_input_tokens), output: $($usage.output_tokens))."
                    }
                }
            }

            if ($agentTurnCompleted -or $agentTurnFailed) {
                break
            }
        }

        if ($agentTurnCompleted -or $agentTurnFailed) {
            if (-not $agentProcess.WaitForExit(10000)) {
                Write-Warning "$agentName finished the repair turn but did not exit within 10 seconds; terminating the stalled CLI process."
                $agentProcess.Kill()
                $agentProcess.WaitForExit()
            }
            if ($agentTurnCompleted) {
                $agentExitCode = 0
            }
            else {
                $agentExitCode = $agentProcess.ExitCode
                if ($agentExitCode -eq 0) {
                    $agentExitCode = 1
                }
            }
        }
        else {
            $agentProcess.WaitForExit()
            $agentExitCode = $agentProcess.ExitCode
            if ($agentExitCode -eq 0) {
                $agentExitCode = 1
            }
        }
    }
    finally {
        if ($agentProcessStarted -and -not $agentProcess.HasExited) {
            $agentProcess.Kill()
            $agentProcess.WaitForExit()
        }
        $agentProcess.Dispose()
    }

    if ($agentExitCode -ne 0) {
        Write-Error "$agentName exited with code $agentExitCode while attempting the fuzz fix."
        exit $agentExitCode
    }

    $currentBranch = (& git -C $repositoryRoot branch --show-current).Trim()
    if ($LASTEXITCODE -ne 0 -or $currentBranch -ne "main") {
        Write-Error "$agentName did not leave the repository on main; refusing to commit."
        exit 1
    }

    $currentHead = (& git -C $repositoryRoot rev-parse HEAD).Trim()
    if ($LASTEXITCODE -ne 0 -or $currentHead -ne $startingHead) {
        Write-Error "$agentName changed Git history; refusing to create another automated commit."
        exit 1
    }

    $repairStatus = @(& git -C $repositoryRoot status --porcelain --untracked-files=all)
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Could not inspect the repair working tree."
        exit 1
    }
    if ($repairStatus.Count -eq 0) {
        Write-Error "$agentName completed without producing a repair diff; nothing will be committed."
        exit 1
    }

    if (-not (Test-Path -LiteralPath $commitOutputPath)) {
        Write-Error "$agentName produced no commit metadata."
        exit 1
    }

    try {
        $commitMetadata = Get-Content -LiteralPath $commitOutputPath -Raw | ConvertFrom-Json
    }
    catch {
        Write-Error "$agentName produced invalid commit metadata: $($_.Exception.Message)"
        exit 1
    }

    $commitSubject = ([string] $commitMetadata.subject).Trim()
    $commitBody = ([string] $commitMetadata.body).Trim()
    if ([string]::IsNullOrWhiteSpace($commitSubject) -or [string]::IsNullOrWhiteSpace($commitBody)) {
        Write-Error "$agentName produced an empty commit subject or body."
        exit 1
    }

    & git -C $repositoryRoot add --all
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Could not stage the fuzz repair."
        exit 1
    }

    & git -C $repositoryRoot diff --cached --quiet
    if ($LASTEXITCODE -eq 0) {
        Write-Error "The fuzz repair produced no staged changes."
        exit 1
    }
    if ($LASTEXITCODE -ne 1) {
        Write-Error "Could not inspect the staged fuzz repair."
        exit 1
    }

    $commitMessage = "$commitSubject`n`n$commitBody`n`n$commitCoAuthor`n"
    [System.IO.File]::WriteAllText($commitMessagePath, $commitMessage, $utf8WithoutBom)

    & git -C $repositoryRoot commit --file $commitMessagePath
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Could not commit the fuzz repair."
        exit 1
    }

    $committedHead = (& git -C $repositoryRoot rev-parse --short HEAD).Trim()
    Write-Host "Committed fuzz repair as $committedHead. Pushing main to origin..."

    & git -C $repositoryRoot push origin main
    if ($LASTEXITCODE -ne 0) {
        Write-Error "The fuzz repair was committed as $committedHead, but pushing origin/main failed."
        exit 1
    }

    $startingHead = (& git -C $repositoryRoot rev-parse HEAD).Trim()
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Could not determine the repaired Git commit before starting another fuzz run."
        exit 1
    }

    Write-Host "$agentName repaired the fuzz failure, committed $committedHead, and pushed origin/main. Starting another fuzz run."
}
finally {
    Remove-Item -LiteralPath $commitOutputPath, $commitSchemaPath, $commitMessagePath -Force -ErrorAction SilentlyContinue
}
}

exit 0
