# Examples:
#   .\scripts\implement-cards.ps1 sos 1 5
#   .\scripts\implement-cards.ps1 sos 1 5 -Runner claude
#   .\scripts\implement-cards.ps1 sos 1 5 -Runner grok

param(
    # The set code to implement cards from, e.g. "sos".
    [Parameter(Mandatory = $true, Position = 0)]
    [string] $SetCode,

    # First collector number to implement (inclusive).
    [Parameter(Mandatory = $true, Position = 1)]
    [int] $From,

    # Last collector number to implement (inclusive).
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

$systemPrompt = "Do not ask clarifying questions, wait for confirmation, or present multiple options. Simply choose the recommended/best approach and implement the code immediately. If I instruct you to implement a card do it even if it require substantial work. be brief with your responses, only mention that is important, save tokens when possible."

$total = $To - $From + 1
$index = 0

Write-Host "Runner: $Runner  Model: $Model"

for ($cardId = $From; $cardId -le $To; $cardId++) {
    $index++
    Write-Host ""
    Write-Host "############################################################"
    Write-Host "# [$index/$total] implement-card $SetCode $cardId"
    Write-Host "############################################################"

    $prompt = "/implement-card $SetCode $cardId"

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
Write-Host "Done. Processed $total card(s) from $SetCode $From to $To."
