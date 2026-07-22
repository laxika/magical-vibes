param(
    [Parameter(Mandatory = $true, Position = 0)]
    [string] $SetId
)

$ErrorActionPreference = "Stop"

function Get-ScryfallSetCards {
    param(
        [Parameter(Mandatory = $true)]
        [string] $SetId
    )

    $cardInfoLauncher = Join-Path $PSScriptRoot "..\mcp\card-info\start.ps1"
    $json = & $cardInfoLauncher get-set $SetId
    if ($LASTEXITCODE -ne 0) {
        throw "Card Info set lookup exited with code $LASTEXITCODE"
    }
    $cachedSet = $json | ConvertFrom-Json
    return @($cachedSet.cards)
}

function Get-CardField {
    param(
        [object] $Card,
        [string] $FieldName
    )

    if ($Card.PSObject.Properties.Name -contains $FieldName -and $null -ne $Card.$FieldName) {
        return $Card.$FieldName
    }

    if ($Card.card_faces -and $Card.card_faces.Count -gt 0) {
        $face = $Card.card_faces[0]
        if ($face.PSObject.Properties.Name -contains $FieldName -and $null -ne $face.$FieldName) {
            return $face.$FieldName
        }
    }

    return $null
}

$setCode = $SetId.ToLowerInvariant()
Write-Host "Loading cards for set '$setCode' through the Card Info cache..."

$scryfallCards = Get-ScryfallSetCards -SetId $setCode
if ($scryfallCards.Count -eq 0) {
    throw "No cards found for set '$setCode'."
}

$cards = foreach ($card in $scryfallCards) {
    [ordered]@{
        setId           = $setCode
        collectorNumber = $card.collector_number
        name            = $card.name
        cardText        = Get-CardField -Card $card -FieldName "oracle_text"
        colors          = @(Get-CardField -Card $card -FieldName "colors")
        manaCost        = Get-CardField -Card $card -FieldName "mana_cost"
        power           = Get-CardField -Card $card -FieldName "power"
        toughness       = Get-CardField -Card $card -FieldName "toughness"
        typeLine        = Get-CardField -Card $card -FieldName "type_line"
    }
}

$output = [ordered]@{
    setId = $setCode
    cards = @($cards)
}

$outputPath = Join-Path $PSScriptRoot "$setCode.json"
$json = $output | ConvertTo-Json -Depth 10
[System.IO.File]::WriteAllText($outputPath, $json, [System.Text.UTF8Encoding]::new($false))

Write-Host "Wrote $($cards.Count) cards to $outputPath"
