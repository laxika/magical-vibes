# Examples:
#   .\scripts\review-cards.ps1 sos 1 5
#   .\scripts\review-cards.ps1 sos 1 5 -Runner claude
#   .\scripts\review-cards.ps1 sos 1 5 -Runner grok
#   .\scripts\review-cards.ps1 sos 1 5 -Runner codex
#   .\scripts\review-cards.ps1 sos 1 5 -Runner muse
#   .\scripts\review-cards.ps1 sos 1 5 -Runner codex -Effort xhigh
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
    [string] $Effort
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

$systemPrompt = "Do not ask clarifying questions, wait for confirmation, or present multiple options. Simply choose the recommended/best approach and review the card immediately. Implementation is read-only: do not edit card classes, effects, predicates, or docs - only delete a stale pass result file when required. Tests are encouraged: when oracle coverage is below 100% or you spot realistic edge cases, ADD focused harness tests (do not rewrite existing ones unless clearly wrong). New failing tests that confirm a bug are good - leave them and report FAIL. Be brief; save tokens when possible. Write scripts/result/{SET}/{collectorNumber}.txt only when there are real issues; on a clean pass delete any stale result file and write nothing."

$total = $To - $From + 1
$index = 0
$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path

if ($Runner -eq "codex") {
    Write-Host "Runner: $Runner  Model: $Model  Effort: $Effort"
}
else {
    Write-Host "Runner: $Runner  Model: $Model"
}

$cardInfoLauncher = Join-Path $PSScriptRoot "..\mcp\card-info\start.ps1"
Write-Host "Warming Card Info cache for $($SetCode.ToUpperInvariant())..."
& $cardInfoLauncher cache-set $SetCode | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Error "Could not populate the Card Info cache for $SetCode."
    exit $LASTEXITCODE
}

for ($cardId = $From; $cardId -le $To; $cardId++) {
    $index++
    Write-Host ""
    Write-Host "############################################################"
    Write-Host "# [$index/$total] review-card $SetCode $cardId"
    Write-Host "############################################################"

    $prompt = "/review-card $SetCode $cardId"

    if ($Runner -eq "grok") {
        & agent -p --force --trust --model $Model "$prompt`n`n$systemPrompt"
    }
    elseif ($Runner -eq "codex") {
        # *>$null needs a non-Stop EAP on Windows PowerShell 5.1, or native stderr
        # aborts/deadlocks under the script-level $ErrorActionPreference=Stop.
        $reasoningConfig = "model_reasoning_effort=`"$Effort`""
        & { $ErrorActionPreference = "Ignore"; & codex --search --ask-for-approval never exec --model $Model --config $reasoningConfig --cd $repositoryRoot "$prompt`n`n$systemPrompt" *>$null }
    }
    else {
        & claude --permission-mode auto --model $Model -p $prompt --append-system-prompt $systemPrompt
    }

    if ($LASTEXITCODE -ne 0) {
        Write-Error "$cliName exited with code $LASTEXITCODE for $SetCode $cardId. Stopping."
        exit $LASTEXITCODE
    }
}

Write-Host ""
Write-Host "Done. Reviewed $total card(s) from $SetCode $From to $To."
Write-Host "Findings (if any) are under scripts/result/<SET>/<collectorNumber>.txt"
