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

    # The Cursor agent model to run. Change if you want a different model.
    [string] $Model = "claude-opus-4-8"
)

$ErrorActionPreference = "Stop"

if ($From -gt $To) {
    Write-Error "From ($From) must be less than or equal to To ($To)."
    exit 1
}

if (-not (Get-Command agent -ErrorAction SilentlyContinue)) {
    Write-Error "The 'agent' CLI was not found on PATH."
    exit 1
}

$total = $To - $From + 1
$index = 0

for ($cardId = $From; $cardId -le $To; $cardId++) {
    $index++
    Write-Host ""
    Write-Host "############################################################"
    Write-Host "# [$index/$total] implement-card $SetCode $cardId"
    Write-Host "############################################################"

    & claude --permission-mode auto --model $Model -p "/implement-card $SetCode $cardId" --append-system-prompt "Do not ask clarifying questions, wait for confirmation, or present multiple options. Simply choose the recommended/best approach and implement the code immediately. If I instruct you to implement a card do it even if it require substantial work. be brief with your responses, only mention that is important, save tokens when possible."

    if ($LASTEXITCODE -ne 0) {
        Write-Error "agent exited with code $LASTEXITCODE for $SetCode $cardId. Stopping."
        exit $LASTEXITCODE
    }
}

Write-Host ""
Write-Host "Done. Processed $total card(s) from $SetCode $From to $To."
