param(
    [Parameter(Mandatory = $true, Position = 0)]
    [string] $SetCode,

    [Parameter(Mandatory = $true, Position = 1)]
    [string] $CollectorNumber,

    [Parameter(Position = 2)]
    [string] $ClassName,

    [switch] $SkipScryfall
)

$ErrorActionPreference = "Stop"

function Write-Section {
    param([string] $Name)
    Write-Host ""
    Write-Host "== $Name =="
}

function Invoke-RepoSearch {
    param(
        [Parameter(Mandatory = $true)]
        [string] $Pattern,

        [Parameter(Mandatory = $true)]
        [string[]] $Paths,

        [int] $MaxResults = 20
    )

    $existingPaths = @($Paths | Where-Object { Test-Path $_ })
    if (-not $existingPaths) {
        return @()
    }

    if (Get-Command rg -ErrorAction SilentlyContinue) {
        $results = & rg -n --no-heading --color never $Pattern @existingPaths 2>$null
        if ($LASTEXITCODE -gt 1) {
            throw "rg failed for pattern '$Pattern'"
        }
        return @($results | Select-Object -First $MaxResults)
    }

    $files = foreach ($path in $existingPaths) {
        Get-ChildItem -Path $path -Recurse -File
    }

    return @($files |
        Select-String -Pattern $Pattern |
        ForEach-Object { "$($_.Path):$($_.LineNumber):$($_.Line.Trim())" } |
        Select-Object -First $MaxResults)
}

function ConvertTo-ClassName {
    param([string] $CardName)
    return ($CardName -replace "[^A-Za-z0-9]", "")
}

function Write-LinesOrNone {
    param(
        [object[]] $Lines,
        [string] $NoneText
    )

    if ($Lines -and $Lines.Count -gt 0) {
        $Lines | ForEach-Object { Write-Host $_ }
    } else {
        Write-Host $NoneText
    }
}

function Get-ScryfallCard {
    param(
        [string] $SetCode,
        [string] $CollectorNumber
    )

    $query = "set:$SetCode cn:$CollectorNumber"
    $encodedQuery = [System.Uri]::EscapeDataString($query)
    $url = "https://api.scryfall.com/cards/search?q=$encodedQuery&format=json"

    $json = & curl.exe -s -L `
        -H "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36" `
        -H "Accept: application/json" `
        $url

    if ($LASTEXITCODE -ne 0) {
        throw "curl.exe exited with code $LASTEXITCODE"
    }

    $response = $json | ConvertFrom-Json
    if ($response.object -eq "error") {
        throw $response.details
    }
    if (-not $response.data -or $response.data.Count -eq 0) {
        throw "No Scryfall result for $query"
    }

    return $response.data[0]
}

function Write-ScryfallSummary {
    param([object] $Card)

    Write-Host "Name: $($Card.name)"
    Write-Host "Mana: $($Card.mana_cost)"
    Write-Host "Type: $($Card.type_line)"
    if ($Card.power -or $Card.toughness) {
        Write-Host "P/T: $($Card.power)/$($Card.toughness)"
    }
    if ($Card.keywords -and $Card.keywords.Count -gt 0) {
        Write-Host "Keywords: $($Card.keywords -join ', ')"
    }
    Write-Host "Oracle:"
    Write-Host $Card.oracle_text
}

function Get-OracleKeywords {
    param([string] $OracleText)

    $keywords = New-Object System.Collections.Generic.List[string]
    if (-not $OracleText) {
        return @()
    }

    if ($OracleText -match "graveyard") { $keywords.Add("graveyard") }
    if ($OracleText -match "top of (your|its owner's|their) library|top of.*library") { $keywords.Add("top.*library") }
    if ($OracleText -match "Draw a card|draw cards?") { $keywords.Add("DrawCardEffect|draw a card") }
    if ($OracleText -match "any number") { $keywords.Add("any number") }
    if ($OracleText -match "up to") { $keywords.Add("up to") }
    if ($OracleText -match "target creature") { $keywords.Add("target creature") }
    if ($OracleText -match "target artifact") { $keywords.Add("target artifact") }
    if ($OracleText -match "return .* from your graveyard") { $keywords.Add("return.*from your graveyard") }
    if ($OracleText -match "destroy target") { $keywords.Add("destroy target") }
    if ($OracleText -match "deals? .* damage") { $keywords.Add("deal.*damage") }
    if ($OracleText -match "counter target") { $keywords.Add("counter target") }
    if ($OracleText -match "exile target") { $keywords.Add("exile target") }
    if ($OracleText -match "search your library") { $keywords.Add("search your library") }
    if ($OracleText -match "create .* token") { $keywords.Add("create.*token") }
    if ($OracleText -match "\+1/\+1 counter") { $keywords.Add("\\+1/\\+1 counter") }

    return @($keywords | Select-Object -Unique)
}

function Get-KnownPatternMatches {
    param([string] $OracleText)

    $knownPatterns = @(
        @{
            Match = "Put any number of target .* cards from your graveyard on top of your library"
            Reference = "FranticSalvage"
            Effect = "PutTargetCardsFromGraveyardOnTopOfLibraryEffect"
        },
        @{
            Match = "Return target creature card from your graveyard to your hand"
            Reference = "Disentomb"
            Effect = "ReturnCardFromGraveyardEffect"
        },
        @{
            Match = "Return .* target .* cards from your graveyard to your hand"
            Reference = "MorbidPlunder"
            Effect = "ReturnTargetCardsFromGraveyardToHandEffect"
        },
        @{
            Match = "Draw a card"
            Reference = "Opt"
            Effect = "DrawCardEffect"
        },
        @{
            Match = "Destroy target creature"
            Reference = "DoomBlade"
            Effect = "DestroyTargetPermanentEffect"
        },
        @{
            Match = "Counter target spell"
            Reference = "Cancel"
            Effect = "CounterTargetSpellEffect"
        },
        @{
            Match = "deals? .* damage to any target"
            Reference = "LightningBolt"
            Effect = "DealDamageToAnyTargetEffect"
        }
    )

    if (-not $OracleText) {
        return @()
    }

    return @($knownPatterns | Where-Object { $OracleText -match $_.Match })
}

function Get-UsagePatterns {
    param(
        [string] $OracleText,
        [object[]] $KnownMatches
    )

    $patterns = New-Object System.Collections.Generic.List[string]
    foreach ($match in $KnownMatches) {
        if ($match.Effect) {
            $patterns.Add($match.Effect)
        }
        if ($match.Reference) {
            $patterns.Add("class\s+$($match.Reference)\s+")
        }
    }

    if ($OracleText -match "Draw a card|draw cards?") { $patterns.Add("DrawCardEffect") }
    if ($OracleText -match "graveyard") { $patterns.Add("canTargetGraveyard|GraveyardEffect|FromGraveyard") }
    if ($OracleText -match "top of .*library") { $patterns.Add("TopOfLibrary|top of.*library") }
    if ($OracleText -match "target creature") { $patterns.Add("CardTypePredicate\(CardType\.CREATURE\)") }

    return @($patterns | Select-Object -Unique)
}

$card = $null
$oracleText = $null

Write-Section "Scryfall"
if ($SkipScryfall) {
    Write-Host "Skipped by -SkipScryfall."
} else {
    try {
        $card = Get-ScryfallCard -SetCode $SetCode -CollectorNumber $CollectorNumber
        $oracleText = $card.oracle_text
        if (-not $ClassName) {
            $ClassName = ConvertTo-ClassName -CardName $card.name
        }
        Write-ScryfallSummary -Card $card
    } catch {
        Write-Host "Lookup failed: $($_.Exception.Message)"
    }
}

if (-not $ClassName) {
    Write-Error "ClassName could not be derived. Re-run with the optional ClassName argument, or allow Scryfall lookup to succeed."
    exit 1
}

Write-Section "Reprint Check"
$classHits = Invoke-RepoSearch -Pattern "class\s+$ClassName\s+" -Paths @("magical-vibes-card/src/main/java") -MaxResults 20
Write-LinesOrNone -Lines $classHits -NoneText "No existing class: $ClassName"

$knownMatches = Get-KnownPatternMatches -OracleText $oracleText
Write-Section "Known Pattern Matches"
if ($knownMatches.Count -gt 0) {
    foreach ($match in $knownMatches) {
        Write-Host "$($match.Reference): $($match.Effect)"
        $referenceClassHits = Invoke-RepoSearch -Pattern "class\s+$($match.Reference)\s+" -Paths @("magical-vibes-card/src/main/java") -MaxResults 10
        $referenceTestHits = Invoke-RepoSearch -Pattern "class\s+$($match.Reference)Test\s+" -Paths @("magical-vibes-backend/src/test/java") -MaxResults 10
        Write-LinesOrNone -Lines $referenceClassHits -NoneText "  No card class file found for $($match.Reference)"
        Write-LinesOrNone -Lines $referenceTestHits -NoneText "  No test class file found for $($match.Reference)Test"
    }
} else {
    Write-Host "No known pattern match."
}

Write-Section "Effect Doc Hits"
$docPaths = @(
    "agent-docs/CARD_PATTERN_INDEX.md",
    "agent-docs/ORACLE_TEXT_EFFECT_MAP.md",
    "agent-docs/EFFECTS_QUICK_REFERENCE.md"
)
$docKeywords = Get-OracleKeywords -OracleText $oracleText
if ($docKeywords.Count -eq 0) {
    Write-Host "No Oracle-derived keywords available."
} else {
    foreach ($keyword in $docKeywords) {
        Write-Host "-- $keyword"
        $hits = Invoke-RepoSearch -Pattern $keyword -Paths $docPaths -MaxResults 12
        Write-LinesOrNone -Lines $hits -NoneText "  No hits."
    }
}

Write-Section "Existing Usages"
$usagePatterns = Get-UsagePatterns -OracleText $oracleText -KnownMatches $knownMatches
if ($usagePatterns.Count -eq 0) {
    Write-Host "No usage patterns available."
} else {
    foreach ($pattern in $usagePatterns) {
        Write-Host "-- $pattern"
        $hits = Invoke-RepoSearch -Pattern $pattern -Paths @(
            "magical-vibes-card/src/main/java",
            "magical-vibes-backend/src/test/java",
            "magical-vibes-domain/src/main/java",
            "magical-vibes-backend/src/main/java"
        ) -MaxResults 15
        Write-LinesOrNone -Lines $hits -NoneText "  No hits."
    }
}

Write-Section "Suggested Files"
$packageLetter = $ClassName.Substring(0, 1).ToLowerInvariant()
Write-Host "Card: magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/$packageLetter/$ClassName.java"
Write-Host "Test: magical-vibes-backend/src/test/java/com/github/laxika/magicalvibes/cards/$packageLetter/${ClassName}Test.java"

Write-Section "Suggested Test Command"
Write-Host "./gradlew :magical-vibes-backend:test --tests `"com.github.laxika.magicalvibes.cards.$packageLetter.${ClassName}Test`""
