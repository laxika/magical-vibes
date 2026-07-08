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
    [string] $Model = "composer-2.5"
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

    & agent --model $Model -p --force "/implement-card $SetCode $cardId"

    if ($LASTEXITCODE -ne 0) {
        Write-Error "agent exited with code $LASTEXITCODE for $SetCode $cardId. Stopping."
        exit $LASTEXITCODE
    }
}

Write-Host ""
Write-Host "Done. Processed $total card(s) from $SetCode $From to $To."
