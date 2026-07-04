param(
    [Parameter(Mandatory = $true, Position = 0)]
    [string] $SetId
)

$ErrorActionPreference = "Stop"

function Invoke-ScryfallApi {
    param(
        [Parameter(Mandatory = $true)]
        [string] $Url
    )

    $response = Invoke-RestMethod -Uri $Url -Method Get -Headers @{
        "User-Agent" = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
        "Accept"     = "application/json"
    }

    if ($response.object -eq "error") {
        throw $response.details
    }

    return $response
}

function Get-ScryfallSetCards {
    param(
        [Parameter(Mandatory = $true)]
        [string] $SetId
    )

    $setCode = $SetId.ToLowerInvariant()
    $query = "set:$setCode"
    $encodedQuery = [System.Uri]::EscapeDataString($query)
    $url = "https://api.scryfall.com/cards/search?q=$encodedQuery&unique=prints&order=set"

    $cards = @()
    do {
        $page = Invoke-ScryfallApi -Url $url
        $pageCards = @($page.data)
        $cards += $pageCards
        $url = if ($page.has_more) { $page.next_page } else { $null }
    } while ($url)

    return $cards
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
Write-Host "Fetching cards for set '$setCode' from Scryfall..."

$scryfallCards = Get-ScryfallSetCards -SetId $setCode
if ($scryfallCards.Count -eq 0) {
    throw "No cards found for set '$setCode'."
}

$cards = foreach ($card in $scryfallCards) {
    [ordered]@{
        name      = $card.name
        cardText  = Get-CardField -Card $card -FieldName "oracle_text"
        colors    = @(Get-CardField -Card $card -FieldName "colors")
        manaCost  = Get-CardField -Card $card -FieldName "mana_cost"
        power     = Get-CardField -Card $card -FieldName "power"
        toughness = Get-CardField -Card $card -FieldName "toughness"
        typeLine  = Get-CardField -Card $card -FieldName "type_line"
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
