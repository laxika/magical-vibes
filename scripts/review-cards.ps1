# Examples:
#   .\scripts\review-cards.ps1 sos 1 5
#   .\scripts\review-cards.ps1 sos 1 5 -Runner claude
#   .\scripts\review-cards.ps1 sos 1 5 -Runner grok

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

    # Which CLI to run: "claude" (default) or "grok" (Cursor agent with Grok).
    [ValidateSet("claude", "grok")]
    [string] $Runner = "claude",

    # Model override. Defaults depend on -Runner:
    #   claude -> claude-opus-4-8
    #   grok   -> cursor-grok-4.5-high
    [string] $Model
)

$ErrorActionPreference = "Stop"

if ($From -gt $To) {
    Write-Error "From ($From) must be less than or equal to To ($To)."
    exit 1
}

if (-not $PSBoundParameters.ContainsKey("Model") -or [string]::IsNullOrWhiteSpace($Model)) {
    $Model = if ($Runner -eq "grok") { "cursor-grok-4.5-high" } else { "claude-opus-4-8" }
}

$cliName = if ($Runner -eq "grok") { "agent" } else { "claude" }
if (-not (Get-Command $cliName -ErrorAction SilentlyContinue)) {
    Write-Error "The '$cliName' CLI was not found on PATH."
    exit 1
}

$systemPrompt = "Do not ask clarifying questions, wait for confirmation, or present multiple options. Simply choose the recommended/best approach and review the card immediately. Implementation is read-only: do not edit card classes, effects, predicates, or docs - only delete a stale pass result file when required. Tests are encouraged: when oracle coverage is below 100% or you spot realistic edge cases, ADD focused harness tests (do not rewrite existing ones unless clearly wrong). New failing tests that confirm a bug are good - leave them and report FAIL. Be brief; save tokens when possible. Write scripts/result/{SET}/{collectorNumber}.txt only when there are real issues; on a clean pass delete any stale result file and write nothing."

$total = $To - $From + 1
$index = 0

Write-Host "Runner: $Runner  Model: $Model"

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
