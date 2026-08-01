# Examples:
#   .\scripts\implement-cards.ps1 sos 1 5
#   .\scripts\implement-cards.ps1 sos 1 5 -Runner claude
#   .\scripts\implement-cards.ps1 sos 1 5 -Runner grok
#   .\scripts\implement-cards.ps1 sos 1 5 -Runner codex
#   .\scripts\implement-cards.ps1 sos 1 5 -Effort xhigh

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

    # Which CLI to run: "claude" (default), "grok" (Cursor agent with Grok),
    # or "codex".
    [ValidateSet("claude", "grok", "codex")]
    [string] $Runner = "claude",

    # Model override. Defaults depend on -Runner:
    #   claude -> claude-opus-5
    #   grok   -> cursor-grok-4.5-high
    #   codex  -> gpt-5.6-sol
    [string] $Model,

    # Reasoning effort for the claude and codex runners. Defaults to "medium"
    # for codex and "low" for claude. Ignored by -Runner grok, which encodes
    # effort in the model name instead.
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
        default { "claude-opus-5" }
    }
}

if (-not $PSBoundParameters.ContainsKey("Effort") -or [string]::IsNullOrWhiteSpace($Effort)) {
    $Effort = if ($Runner -eq "codex") { "xhigh" } else { "low" }
}

if ($Runner -eq "grok" -and $PSBoundParameters.ContainsKey("Effort")) {
    Write-Warning "-Effort is only supported by the claude and codex runners; ignoring it for grok."
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

$systemPrompt = "Do not ask clarifying questions, wait for confirmation, or present multiple options. Simply choose the recommended/best approach and implement the code immediately. If I instruct you to implement a card do it even if it require substantial work. be brief with your responses, only mention that is important, save tokens when possible."

$total = $To - $From + 1
$index = 0
$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path

if ($Runner -eq "grok") {
    Write-Host "Runner: $Runner  Model: $Model"
}
else {
    Write-Host "Runner: $Runner  Model: $Model  Effort: $Effort"
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
    Write-Host "# [$index/$total] implement-card $SetCode $cardId"
    Write-Host "############################################################"

    $prompt = "/implement-card $SetCode $cardId"

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
        & claude --permission-mode auto --model $Model --effort $Effort -p $prompt --append-system-prompt $systemPrompt
    }

    if ($LASTEXITCODE -ne 0) {
        Write-Error "$cliName exited with code $LASTEXITCODE for $SetCode $cardId. Stopping."
        exit $LASTEXITCODE
    }
}

Write-Host ""
Write-Host "Done. Processed $total card(s) from $SetCode $From to $To."
