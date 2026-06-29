param(
    [Parameter(Mandatory = $true, Position = 0)]
    [string] $SetCode,

    [Parameter(Mandatory = $true, Position = 1)]
    [string] $CollectorNumber,

    [Parameter(Position = 2)]
    [string] $ClassName,

    [switch] $SkipScryfall,

    [switch] $Full
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

function Find-ClassFile {
    param(
        [Parameter(Mandatory = $true)]
        [string] $ClassName,

        [Parameter(Mandatory = $true)]
        [string] $RootPath
    )

    if (-not (Test-Path $RootPath)) {
        return $null
    }

    $expectedFileName = "$ClassName.java"
    $exactFile = Get-ChildItem -Path $RootPath -Recurse -File -Filter $expectedFileName -ErrorAction SilentlyContinue |
        Select-Object -First 1
    if ($exactFile) {
        return $exactFile.FullName.Replace((Get-Location).Path + [System.IO.Path]::DirectorySeparatorChar, "")
    }

    $hits = Invoke-RepoSearch -Pattern "class\s+$ClassName\s+" -Paths @($RootPath) -MaxResults 1
    if ($hits.Count -eq 0) {
        return $null
    }

    return ($hits[0] -replace ":\d+:.*$", "")
}

function ConvertTo-ClassName {
    param([string] $CardName)
    return ($CardName -replace "[^A-Za-z0-9]", "")
}

function ConvertTo-IntOrNull {
    param([string] $Value)

    $normalized = $Value.ToLowerInvariant()
    $wordNumbers = @{
        "one" = 1
        "two" = 2
        "three" = 3
        "four" = 4
        "five" = 5
        "six" = 6
        "seven" = 7
        "eight" = 8
        "nine" = 9
        "ten" = 10
    }
    if ($wordNumbers.ContainsKey($normalized)) {
        return $wordNumbers[$normalized]
    }

    $number = 0
    if ([int]::TryParse($normalized, [ref] $number)) {
        return $number
    }

    return $null
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

    $dynamicPatterns = New-Object System.Collections.Generic.List[hashtable]
    if ($OracleText -match "^Target player draws (\w+) cards? and loses (\w+) life\.$") {
        $drawAmount = ConvertTo-IntOrNull $Matches[1]
        $lifeLoss = ConvertTo-IntOrNull $Matches[2]
        if ($drawAmount -ne $null -and $lifeLoss -ne $null) {
            $dynamicPatterns.Add(@{
                Match = "^Target player draws \w+ cards? and loses \w+ life\.$"
                Label = "Target player draws N cards and loses M life"
                Reference = "SignInBlood"
                Effect = "DrawCardForTargetPlayerEffect + TargetPlayerLosesLifeEffect"
                TargetFilter = "PlayerPredicateTargetFilter(new PlayerRelationPredicate(PlayerRelation.ANY), `"Target must be a player`")"
                Imports = @(
                    "com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect",
                    "com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeEffect",
                    "com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter",
                    "com.github.laxika.magicalvibes.model.filter.PlayerRelation",
                    "com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate"
                )
                ConstructorLines = @(
                    "target(new PlayerPredicateTargetFilter(",
                    "        new PlayerRelationPredicate(PlayerRelation.ANY),",
                    "        `"Target must be a player`"",
                    "))",
                    "        .addEffect(EffectSlot.SPELL, new DrawCardForTargetPlayerEffect($drawAmount))",
                    "        .addEffect(EffectSlot.SPELL, new TargetPlayerLosesLifeEffect($lifeLoss));"
                )
                TestNotes = @(
                    "assert EffectResolution.needsTarget(card)",
                    "assert target filter is PlayerPredicateTargetFilter",
                    "assert SPELL effects are DrawCardForTargetPlayerEffect($drawAmount) then TargetPlayerLosesLifeEffect($lifeLoss)",
                    "cast with castSorcery targeting opponent and assert hand +$drawAmount and life -$lifeLoss",
                    "cast targeting self and assert the caster draws $drawAmount after the spell leaves hand and loses $lifeLoss life",
                    "assert a creature target is rejected"
                )
            })
        }
    }

    $staticPatterns = @(
        @{
            Match = "Put target creature on top of its owner's library"
            Reference = "Excommunicate"
            Effect = "PutTargetOnTopOfLibraryEffect"
            TargetFilter = "PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), `"Target must be a creature`")"
            Imports = @(
                "com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect",
                "com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate",
                "com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter"
            )
            ConstructorLines = @(
                "target(new PermanentPredicateTargetFilter(",
                "        new PermanentIsCreaturePredicate(),",
                "        `"Target must be a creature`"",
                ")).addEffect(EffectSlot.SPELL, new PutTargetOnTopOfLibraryEffect());"
            )
            TestNotes = @(
                "assert EffectResolution.needsTarget(card)",
                "assert target filter equals creature-only PermanentPredicateTargetFilter",
                "cast with castInstant/castSorcery according to type_line and assert stack entry type",
                "assert noncreature permanent target is rejected; include another valid creature so the card is playable",
                "resolve and assert target leaves battlefield, is not in graveyard, and is deck.getFirst() for its owner",
                "remove target before resolution and assert fizzle plus unchanged owner library size"
            )
        },
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

    return @(($dynamicPatterns + $staticPatterns) | Where-Object { $OracleText -match $_.Match })
}

function Write-FastPathTemplate {
    param(
        [object[]] $KnownMatches,
        [string] $ClassName,
        [string] $SetCode,
        [string] $CollectorNumber,
        [string] $PackageLetter
    )

    $templatedMatches = @($KnownMatches | Where-Object { $_.ConstructorLines -and $_.Imports })
    if ($templatedMatches.Count -eq 0) {
        Write-Host "No exact fast-path template available."
        return $false
    }
    if ($templatedMatches.Count -gt 1) {
        Write-Host "Multiple possible templates found; inspect known pattern matches before using one."
        return $false
    }

    $match = $templatedMatches[0]
    Write-Host "Confidence: exact known pattern"
    Write-Host "Reference: $($match.Reference)"
    Write-Host "Effect: $($match.Effect)"
    if ($match.TargetFilter) {
        Write-Host "Target filter: $($match.TargetFilter)"
    }

    Write-Host ""
    Write-Host "Card skeleton:"
    Write-Host "package com.github.laxika.magicalvibes.cards.$PackageLetter;"
    Write-Host ""
    Write-Host "import com.github.laxika.magicalvibes.cards.CardRegistration;"
    Write-Host "import com.github.laxika.magicalvibes.model.Card;"
    Write-Host "import com.github.laxika.magicalvibes.model.EffectSlot;"
    foreach ($import in $match.Imports) {
        Write-Host "import $import;"
    }
    Write-Host ""
    Write-Host "@CardRegistration(set = `"$($SetCode.ToUpperInvariant())`", collectorNumber = `"$CollectorNumber`")"
    Write-Host "public class $ClassName extends Card {"
    Write-Host ""
    Write-Host "    public $ClassName() {"
    foreach ($line in $match.ConstructorLines) {
        Write-Host "        $line"
    }
    Write-Host "    }"
    Write-Host "}"

    if ($match.TestNotes -and $match.TestNotes.Count -gt 0) {
        Write-Host ""
        Write-Host "Minimal test checklist:"
        foreach ($note in $match.TestNotes) {
            Write-Host "- $note"
        }
    }

    return $true
}

function Test-IsBasicLand {
    param([object] $Card)

    return $Card -and $Card.type_line -match "\bBasic\b" -and $Card.type_line -match "\bLand\b"
}

function Test-IsVanillaCard {
    param([object] $Card)

    if (-not $Card) {
        return $false
    }

    $hasNoOracleText = [string]::IsNullOrWhiteSpace($Card.oracle_text)
    $hasNoKeywords = -not $Card.keywords -or $Card.keywords.Count -eq 0
    return $hasNoOracleText -and $hasNoKeywords -and -not (Test-IsBasicLand -Card $Card)
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
        $referenceCardFile = Find-ClassFile -ClassName $match.Reference -RootPath "magical-vibes-card/src/main/java"
        $referenceTestFile = Find-ClassFile -ClassName "$($match.Reference)Test" -RootPath "magical-vibes-application/src/test/java"
        if ($referenceCardFile) {
            Write-Host "  Reference card file: $referenceCardFile"
        } else {
            Write-Host "  No card class file found for $($match.Reference)"
        }
        if ($referenceTestFile) {
            Write-Host "  Reference test file: $referenceTestFile"
        } else {
            Write-Host "  No test class file found for $($match.Reference)Test"
        }
        if ($match.ConstructorLines -and $match.ConstructorLines.Count -gt 0) {
            Write-Host "  Constructor example:"
            foreach ($line in $match.ConstructorLines) {
                Write-Host "    $line"
            }
        }
    }
} else {
    Write-Host "No known pattern match."
}

$packageLetter = $ClassName.Substring(0, 1).ToLowerInvariant()

Write-Section "Implementation Guidance"
if (Test-IsBasicLand -Card $card) {
    Write-Host "Tests: skip (basic land)."
} elseif (Test-IsVanillaCard -Card $card) {
    Write-Host "Tests: skip (vanilla card with no engine behavior)."
} else {
    Write-Host "Tests: write focused card behavior tests."
}
if ($knownMatches.Count -gt 0) {
    $primaryMatch = @($knownMatches)[0]
    Write-Host "Closest pattern: $($primaryMatch.Reference)"
    $primaryReferenceCardFile = Find-ClassFile -ClassName $primaryMatch.Reference -RootPath "magical-vibes-card/src/main/java"
    $primaryReferenceTestFile = Find-ClassFile -ClassName "$($primaryMatch.Reference)Test" -RootPath "magical-vibes-application/src/test/java"
    if ($primaryReferenceCardFile) {
        Write-Host "Closest card file: $primaryReferenceCardFile"
    }
    if ($primaryReferenceTestFile) {
        Write-Host "Closest test file: $primaryReferenceTestFile"
    }
    Write-Host "Effect constructors: $($primaryMatch.Effect)"
} else {
    Write-Host "Closest pattern: none found; inspect Effect Doc Hits/Existing Usages below."
}

Write-Section "Fast Path Template"
$hasFastPathTemplate = $false
if ($classHits.Count -gt 0) {
    Write-Host "Existing class found. Prefer adding a @CardRegistration for this printing if it is the same Oracle card."
} else {
    $hasFastPathTemplate = Write-FastPathTemplate `
        -KnownMatches $knownMatches `
        -ClassName $ClassName `
        -SetCode $SetCode `
        -CollectorNumber $CollectorNumber `
        -PackageLetter $packageLetter
}

$skipBroadContext = ($hasFastPathTemplate -or $classHits.Count -gt 0) -and -not $Full
if ($skipBroadContext) {
    Write-Section "Broad Context"
    if ($classHits.Count -gt 0) {
        Write-Host "Skipped because an existing class was found. Re-run with -Full to include Effect Doc Hits and Existing Usages."
    } else {
        Write-Host "Skipped because an exact fast-path template was found. Re-run with -Full to include Effect Doc Hits and Existing Usages."
    }
} else {
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
                "magical-vibes-application/src/test/java",
                "magical-vibes-domain/src/main/java",
                "magical-vibes-engine/src/main/java"
            ) -MaxResults 15
            Write-LinesOrNone -Lines $hits -NoneText "  No hits."
        }
    }
}

Write-Section "Suggested Files"
Write-Host "Card: magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/$packageLetter/$ClassName.java"
Write-Host "Test: magical-vibes-application/src/test/java/com/github/laxika/magicalvibes/cards/$packageLetter/${ClassName}Test.java"

Write-Section "Suggested Test Command"
Write-Host "./gradlew :magical-vibes-application:test --tests `"com.github.laxika.magicalvibes.cards.$packageLetter.${ClassName}Test`""
