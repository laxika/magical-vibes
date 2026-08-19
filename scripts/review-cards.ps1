# Examples:
#   .\scripts\review-cards.ps1 sos 1 5
#   .\scripts\review-cards.ps1 sos 1 5 -Runner claude
#   .\scripts\review-cards.ps1 sos 1 5 -Runner grok
#   .\scripts\review-cards.ps1 sos 1 5 -Runner codex
#   .\scripts\review-cards.ps1 sos 1 5 -Runner muse
#   .\scripts\review-cards.ps1 sos 1 5 -Runner codex -Effort xhigh
#   .\scripts\review-cards.ps1 sos 1 20 -Parallelism 10
#
# The muse runner drives the claude CLI against Meta's Muse endpoint and needs
# $env:MODEL_API_KEY to be set first:
#   $env:MODEL_API_KEY = "<your key>"

param(
    # The set code to review cards from, e.g. "sos".
    [Parameter(Mandatory = $true, Position = 0)]
    [string] $SetCode,

    # First collector number to review (inclusive).
    [Parameter(Mandatory = $true, Position = 1)]
    [int] $From,

    # Last collector number to review (inclusive).
    [Parameter(Mandatory = $true, Position = 2)]
    [int] $To,

    # Which CLI to run: "claude" (default), "grok" (Cursor agent with Grok),
    # "codex", or "muse" (the claude CLI pointed at Meta's Muse endpoint).
    [ValidateSet("claude", "grok", "codex", "muse")]
    [string] $Runner = "claude",

    # Model override. Defaults depend on -Runner:
    #   claude -> claude-opus-4-8
    #   grok   -> cursor-grok-4.5-high
    #   codex  -> gpt-5.6-luna
    #   muse   -> muse-spark-1.2-contributor
    [string] $Model,

    # Reasoning effort for the codex runner. Defaults to "xhigh" and is ignored
    # by the other runners.
    [ValidateSet("low", "medium", "high", "xhigh", "max")]
    [string] $Effort,

    # Maximum number of card reviews to run at the same time. Use 1 to run
    # reviews sequentially.
    [ValidateRange(1, 100)]
    [int] $Parallelism = 10
)

$ErrorActionPreference = "Stop"

if ($From -gt $To) {
    Write-Error "From ($From) must be less than or equal to To ($To)."
    exit 1
}

if (-not $PSBoundParameters.ContainsKey("Model") -or [string]::IsNullOrWhiteSpace($Model)) {
    $Model = switch ($Runner) {
        "grok" { "cursor-grok-4.5-high" }
        "codex" { "gpt-5.6-luna" }
        "muse" { "muse-spark-1.2-contributor" }
        default { "claude-opus-4-8" }
    }
}

if (-not $PSBoundParameters.ContainsKey("Effort") -or [string]::IsNullOrWhiteSpace($Effort)) {
    $Effort = "xhigh"
}

if ($Runner -ne "codex" -and $PSBoundParameters.ContainsKey("Effort")) {
    Write-Warning "-Effort is only supported by the codex runner; ignoring it for $Runner."
}

$cliName = switch ($Runner) {
    "grok" { "agent" }
    "codex" { "codex" }
    default { "claude" }
}
if (-not (Get-Command $cliName -ErrorAction SilentlyContinue)) {
    Write-Error "The '$cliName' CLI was not found on PATH."
    exit 1
}

if ($Runner -eq "muse") {
    if ([string]::IsNullOrWhiteSpace($env:MODEL_API_KEY)) {
        Write-Error "The muse runner needs an API key. Set it first with: `$env:MODEL_API_KEY = `"<your key>`""
        exit 1
    }

    $env:ANTHROPIC_BASE_URL = "https://api.meta.ai"
    $env:ANTHROPIC_AUTH_TOKEN = $env:MODEL_API_KEY
    $env:ANTHROPIC_MODEL = $Model
    $env:ANTHROPIC_DEFAULT_OPUS_MODEL = $Model
    $env:ANTHROPIC_DEFAULT_SONNET_MODEL = $Model
    $env:ANTHROPIC_DEFAULT_HAIKU_MODEL = $Model
    $env:CLAUDE_CODE_SUBAGENT_MODEL = $Model
    $env:ENABLE_TOOL_SEARCH = "true"
}

$systemPrompt = "Do not ask clarifying questions, wait for confirmation, or present multiple options. Simply choose the recommended/best approach and review the card immediately. Be thorough in the review. Implementation is read-only: do not edit card classes, effects, predicates, or docs - only delete a stale pass result file when required. Review every existing test for the card under review for rules accuracy and correctness. Ensure every test class for the card under review has @CardUsed({...}) listing every concrete card class it constructs, including support cards; add or correct class- and method-level annotations as needed. This annotation maintenance is required and is an exception to the read-only and test-modification restrictions. Make sure that only one set's cards are used in the test (preferably the set where the tested card is from). If it is not possible, then the test could use other ones as well, but we should try to stick to a limited number of sets if possible. Fix or otherwise modify an existing test only when it is wrong. Also inspect the current test harness for higher-level helpers. When an existing helper can replace multiple lines in the card's tests without changing their behavior or coverage, refactor those tests to use it instead of duplicating lower-level steps; this cleanup is an exception to the preceding restriction. Tests are encouraged: when oracle coverage is below 100% or you spot realistic edge cases, ADD focused harness tests. Always look up cards used for testing. Verify their real mana costs and other parameters using the MCP. Whenever possible, use real cards for testing. New failing tests that confirm a bug are good - leave them and report FAIL. Write scripts/result/{SET}/{collectorNumber}.txt only when there are real issues; on a clean pass delete any stale result file and write nothing."

$total = $To - $From + 1
$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$effectiveParallelism = [Math]::Min($Parallelism, $total)

if ($Runner -eq "codex") {
    Write-Host "Runner: $Runner  Model: $Model  Effort: $Effort  Parallelism: $effectiveParallelism"
}
else {
    Write-Host "Runner: $Runner  Model: $Model  Parallelism: $effectiveParallelism"
}

$cardInfoLauncher = Join-Path $PSScriptRoot "..\mcp\card-info\start.ps1"
Write-Host "Warming Card Info cache for $($SetCode.ToUpperInvariant())..."
& $cardInfoLauncher cache-set $SetCode | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Error "Could not populate the Card Info cache for $SetCode."
    exit $LASTEXITCODE
}

$reviewJob = {
    param(
        [string] $JobRunner,
        [string] $JobModel,
        [string] $JobEffort,
        [string] $JobRepositoryRoot,
        [string] $JobSetCode,
        [int] $JobCardId,
        [string] $JobSystemPrompt
    )

    try {
        Set-Location -LiteralPath $JobRepositoryRoot
        $prompt = "/review-card $JobSetCode $JobCardId"
        $commandOutput = @()

        if ($JobRunner -eq "grok") {
            $commandOutput = @(& agent -p --force --trust --model $JobModel "$prompt`n`n$JobSystemPrompt" 2>&1)
            $exitCode = $LASTEXITCODE
        }
        elseif ($JobRunner -eq "codex") {
            # A non-Stop EAP prevents native stderr from aborting or deadlocking
            # under Windows PowerShell 5.1. Codex output is intentionally quiet.
            $ErrorActionPreference = "Continue"
            $reasoningConfig = "model_reasoning_effort=`"$JobEffort`""
            & codex --search --ask-for-approval never exec --model $JobModel --config $reasoningConfig --cd $JobRepositoryRoot "$prompt`n`n$JobSystemPrompt" *>$null
            $exitCode = $LASTEXITCODE
        }
        else {
            $commandOutput = @(& claude --permission-mode auto --model $JobModel -p $prompt --append-system-prompt $JobSystemPrompt 2>&1)
            $exitCode = $LASTEXITCODE
        }

        [pscustomobject] @{
            CardId = $JobCardId
            ExitCode = $exitCode
            Output = @($commandOutput | ForEach-Object { $_.ToString() })
        }
    }
    catch {
        [pscustomobject] @{
            CardId = $JobCardId
            ExitCode = 1
            Output = @($_.Exception.Message)
        }
    }
}

$runningJobs = @{}
$cardIdsByJobId = @{}
$nextCardId = $From
$started = 0
$completed = 0
$failure = $null

try {
    while ($runningJobs.Count -gt 0 -or ($nextCardId -le $To -and $null -eq $failure)) {
        while ($null -eq $failure -and $runningJobs.Count -lt $effectiveParallelism -and $nextCardId -le $To) {
            $started++
            Write-Host ""
            Write-Host "############################################################"
            Write-Host "# Starting [$started/$total] review-card $SetCode $nextCardId"
            Write-Host "############################################################"

            $job = Start-Job -ScriptBlock $reviewJob -ArgumentList @(
                $Runner,
                $Model,
                $Effort,
                $repositoryRoot,
                $SetCode,
                $nextCardId,
                $systemPrompt
            )
            $runningJobs[$job.Id] = $job
            $cardIdsByJobId[$job.Id] = $nextCardId
            $nextCardId++
        }

        if ($runningJobs.Count -eq 0) {
            break
        }

        $completedJob = Wait-Job -Job @($runningJobs.Values) -Any
        $completedCardId = $cardIdsByJobId[$completedJob.Id]
        try {
            $result = Receive-Job -Job $completedJob
        }
        catch {
            $result = [pscustomobject] @{
                CardId = $completedCardId
                ExitCode = 1
                Output = @($_.Exception.Message)
            }
        }
        if ($null -eq $result) {
            $result = [pscustomobject] @{
                CardId = $completedCardId
                ExitCode = 1
                Output = @("Review job ended without returning a result. Job state: $($completedJob.State).")
            }
        }
        $runningJobs.Remove($completedJob.Id)
        $cardIdsByJobId.Remove($completedJob.Id)
        Remove-Job -Job $completedJob
        $completed++

        Write-Host ""
        Write-Host "############################################################"
        Write-Host "# Completed [$completed/$total] review-card $SetCode $($result.CardId)"
        Write-Host "############################################################"
        foreach ($line in @($result.Output)) {
            Write-Host $line
        }

        if ($result.ExitCode -ne 0) {
            Write-Warning "$cliName exited with code $($result.ExitCode) for $SetCode $($result.CardId). No new reviews will be started."
            if ($null -eq $failure) {
                $failure = $result
            }
        }
    }
}
finally {
    foreach ($job in @($runningJobs.Values)) {
        if ($job.State -eq "Running") {
            Stop-Job -Job $job
        }
        Receive-Job -Job $job | Out-Null
        Remove-Job -Job $job -Force
    }
}

if ($null -ne $failure) {
    Write-Error "Review failed for $SetCode $($failure.CardId) with exit code $($failure.ExitCode). Already-running reviews were allowed to finish."
    exit $failure.ExitCode
}

Write-Host ""
Write-Host "Done. Reviewed $total card(s) from $SetCode $From to $To."
Write-Host "Findings (if any) are under scripts/result/<SET>/<collectorNumber>.txt"
