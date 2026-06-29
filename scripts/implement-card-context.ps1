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
        foreach ($effectClass in @($match.EffectClasses)) {
            if ($effectClass) {
                $patterns.Add($effectClass)
            }
        }
    }

    if ($OracleText -match "Draw a card|draw cards?") { $patterns.Add("DrawCardEffect") }
    if ($OracleText -match "graveyard") { $patterns.Add("canTargetGraveyard|GraveyardEffect|FromGraveyard") }
    if ($OracleText -match "top of .*library") { $patterns.Add("TopOfLibrary|top of.*library") }
    if ($OracleText -match "target creature") { $patterns.Add("CardTypePredicate\(CardType\.CREATURE\)") }

    return @($patterns | Select-Object -Unique)
}

function ConvertTo-CardSubtypeEnum {
    param([string] $SubtypeName)
    return ($SubtypeName.ToUpperInvariant() -replace "[^A-Za-z0-9]+", "_")
}

function Find-EffectClassFile {
    param([string] $EffectClassName)

    $domainRoot = "magical-vibes-domain/src/main/java/com/github/laxika/magicalvibes/model/effect"
    $path = Join-Path $domainRoot "$EffectClassName.java"
    if (Test-Path $path) {
        return $path.Replace((Get-Location).Path + [System.IO.Path]::DirectorySeparatorChar, "")
    }
    return $null
}

function Find-EffectHandlerFile {
    param([string] $EffectClassName)

    $handlerName = "${EffectClassName}Handler"
    $engineRoot = "magical-vibes-engine/src/main/java"
    if (-not (Test-Path $engineRoot)) {
        return $null
    }

    $hit = Get-ChildItem -Path $engineRoot -Recurse -File -Filter "$handlerName.java" -ErrorAction SilentlyContinue |
        Select-Object -First 1
    if ($hit) {
        return $hit.FullName.Replace((Get-Location).Path + [System.IO.Path]::DirectorySeparatorChar, "")
    }
    return $null
}

function Get-ConstructorSnippet {
    param([string] $FilePath)

    if (-not $FilePath -or -not (Test-Path $FilePath)) {
        return @()
    }

    $lines = Get-Content $FilePath
    $start = -1
    for ($i = 0; $i -lt $lines.Count; $i++) {
        if ($lines[$i] -match 'public\s+\w+\s*\(\s*\)\s*\{') {
            $start = $i
            break
        }
    }
    if ($start -lt 0) {
        return @()
    }

    $depth = 0
    $snippet = New-Object System.Collections.Generic.List[string]
    for ($i = $start; $i -lt $lines.Count; $i++) {
        $line = $lines[$i]
        $snippet.Add($line)
        $depth += @($line.ToCharArray() | Where-Object { $_ -eq '{' }).Count
        $depth -= @($line.ToCharArray() | Where-Object { $_ -eq '}' }).Count
        if ($depth -eq 0 -and $i -gt $start) {
            break
        }
    }

    return @($snippet)
}

function Get-OracleFragmentDefinitions {
    param([string] $OracleText)

    $fragments = New-Object System.Collections.Generic.List[hashtable]

    if ($OracleText -match '(?i)enters( the battlefield)? tapped( unless)?') {
        if ($Matches[2]) {
            # Handled by the unless-specific static fragment below.
        } else {
            $fragments.Add(@{
                Label = "Enters tapped"
                Reference = "AltarOfTheLost"
                EffectClasses = @("EntersTappedEffect")
                Imports = @("com.github.laxika.magicalvibes.model.effect.EntersTappedEffect")
                ConstructorLines = @("addEffect(EffectSlot.STATIC, new EntersTappedEffect());")
                TestNotes = @("assert enters the battlefield tapped")
            })
        }
    }

    if ($OracleText -match '(?i)\{T\}: Add \{C\}') {
        $fragments.Add(@{
            Label = "Tap for colorless mana"
            Reference = "VaultOfTheArchangel"
            EffectClasses = @("AwardManaEffect")
            Imports = @(
                "com.github.laxika.magicalvibes.model.ActivatedAbility",
                "com.github.laxika.magicalvibes.model.ManaColor",
                "com.github.laxika.magicalvibes.model.effect.AwardManaEffect",
                "java.util.List"
            )
            ConstructorLines = @(
                'addActivatedAbility(new ActivatedAbility(',
                '        true,',
                '        null,',
                '        List.of(new AwardManaEffect(ManaColor.COLORLESS)),',
                '        "{T}: Add {C}."',
                '));'
            )
            TestNotes = @("assert mana ability adds colorless mana when tapped")
        })
    }

    if ($OracleText -match '(?i)During turns other than yours.+?(\d+)/(\d+)\s+([A-Za-z]+)\s+artifact creature with flying') {
        $subtype = ConvertTo-CardSubtypeEnum $Matches[3]
        $power = $Matches[1]
        $toughness = $Matches[2]
        $fragments.Add(@{
            Label = "Opponent-turn self-animate ($power/$toughness $subtype with flying)"
            Reference = "WardenOfTheWall"
            EffectClasses = @("NotControllerTurnConditionalEffect", "AnimateSelfWithStatsEffect")
            Imports = @(
                "com.github.laxika.magicalvibes.model.CardSubtype",
                "com.github.laxika.magicalvibes.model.Keyword",
                "com.github.laxika.magicalvibes.model.effect.AnimateSelfWithStatsEffect",
                "com.github.laxika.magicalvibes.model.effect.NotControllerTurnConditionalEffect",
                "java.util.List",
                "java.util.Set"
            )
            ConstructorLines = @(
                "addEffect(EffectSlot.STATIC, new NotControllerTurnConditionalEffect(",
                "        new AnimateSelfWithStatsEffect($power, $toughness, List.of(CardSubtype.$subtype), Set.of(Keyword.FLYING))));"
            )
            TestNotes = @(
                "assert not a creature during controller's turn",
                "assert 2/3 flying Gargoyle on opponent's turn (forceActivePlayer)",
                "assert toggles when active player changes",
                "assert remains an artifact while animated"
            )
        })
    }

    if ($OracleText -match '(?i)if you control three or more artifacts.+?(\d+)/(\d+)\s+([A-Za-z]+)') {
        $subtype = ConvertTo-CardSubtypeEnum $Matches[3]
        $power = $Matches[1]
        $toughness = $Matches[2]
        $fragments.Add(@{
            Label = "Metalcraft self-animate ($power/$toughness $subtype)"
            Reference = "RustedRelic"
            EffectClasses = @("MetalcraftConditionalEffect", "AnimateSelfWithStatsEffect")
            Imports = @(
                "com.github.laxika.magicalvibes.model.CardSubtype",
                "com.github.laxika.magicalvibes.model.effect.AnimateSelfWithStatsEffect",
                "com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect",
                "java.util.List",
                "java.util.Set"
            )
            ConstructorLines = @(
                "addEffect(EffectSlot.STATIC, new MetalcraftConditionalEffect(",
                "        new AnimateSelfWithStatsEffect($power, $toughness, List.of(CardSubtype.$subtype), Set.of())));"
            )
            TestNotes = @(
                "assert not a creature below 3 artifacts",
                "assert becomes creature at 3+ artifacts you control",
                "assert opponent artifacts do not count"
            )
        })
    }

    if ($OracleText -match '(?i)Creatures you control get \+(\d+)/\+(\d+) until end of turn') {
        $power = $Matches[1]
        $toughness = $Matches[2]
        $fragments.Add(@{
            Label = "Pump all own creatures until EOT (+$power/+$toughness)"
            Reference = "Charge"
            EffectClasses = @("BoostAllOwnCreaturesEffect")
            Imports = @("com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect")
            ConstructorLines = @("addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect($power, $toughness));")
            TestNotes = @(
                "assert own creatures get boost after resolution",
                "assert opponent creatures unaffected",
                "assert boost wears off at cleanup"
            )
        })
    }

    $staticFragments = @(
        @{
            Match = '(?i)enters the battlefield tapped unless you control'
            Label = "Enters tapped unless condition"
            Reference = "DragonskullSummit"
            EffectClasses = @("EntersTappedUnlessControlsPermanentEffect")
        },
        @{
            Match = '(?i)During your turn.+has first strike'
            Label = "First strike during your turn (equipped)"
            Reference = "JoustingLance"
            EffectClasses = @("ControllerTurnConditionalEffect", "GrantKeywordEffect")
        },
        @{
            Match = '(?i)When .+ enters the battlefield'
            Label = "ETB triggered ability"
            Reference = "BriarpackAlpha"
            EffectClasses = @()
            TestNotes = @(
                "inspect reference ETB card for exact effect + targeting",
                "assert trigger fires on ETB and resolves correctly",
                "assert fizzle when target becomes illegal before resolution"
            )
        },
        @{
            Match = '(?i)Equip \{'
            Label = "Equipment with equip cost"
            Reference = "LeoninScimitar"
            EffectClasses = @("EquipAbility")
            TestNotes = @("assert equip attaches to creature and grants static effects")
        },
        @{
            Match = '(?i)Flashback'
            Label = "Flashback"
            Reference = "MomentaryBlink"
            EffectClasses = @("FlashbackAbility")
        },
        @{
            Match = '(?i)Crew \d+'
            Label = "Vehicle crew"
            Reference = "SmugglersCopter"
            EffectClasses = @("CrewAbility", "AnimateSelfAsCreatureEffect")
        }
    )

    foreach ($fragment in $staticFragments) {
        if ($OracleText -match $fragment.Match) {
            $copy = @{}
            $fragment.Keys | ForEach-Object { $copy[$_] = $fragment[$_] }
            if ($copy.ContainsKey("ConstructorLines") -and $copy.ConstructorLines -is [string[]]) {
                $resolved = @()
                foreach ($line in $copy.ConstructorLines) {
                    $resolved += [regex]::Replace($line, '\$\(Matches\[(\d+)\]\)', {
                        param($m)
                        $idx = [int]$m.Groups[1].Value
                        if ($Matches.Count -gt $idx) { return $Matches[$idx] }
                        return $m.Value
                    })
                }
                $copy.ConstructorLines = $resolved
            }
            $fragments.Add($copy)
        }
    }

    return @($fragments)
}

function Get-OracleFragments {
    param(
        [string] $OracleText,
        [object[]] $KnownMatches
    )

    $fragments = New-Object System.Collections.Generic.List[hashtable]
    foreach ($fragment in Get-OracleFragmentDefinitions -OracleText $OracleText) {
        $fragments.Add($fragment)
    }

    foreach ($match in $KnownMatches) {
        if ($match.ConstructorLines -and $match.Imports) {
            $fragments.Add(@{
                Label = $match.Label
                Reference = $match.Reference
                EffectClasses = @()
                Effect = $match.Effect
                Imports = $match.Imports
                ConstructorLines = $match.ConstructorLines
                TestNotes = $match.TestNotes
            })
        }
    }

    return @($fragments)
}

function Write-OracleEffectMapping {
    param([object[]] $OracleFragments)

    $nonDomainClasses = @("ActivatedAbility", "EquipAbility", "FlashbackAbility", "CrewAbility")

    if ($OracleFragments.Count -eq 0) {
        Write-Host "No oracle fragments mapped."
        return
    }

    foreach ($fragment in $OracleFragments) {
        $label = if ($fragment.Label) { $fragment.Label } else { $fragment.Reference }
        Write-Host "-- $label"
        if ($fragment.Effect) {
            Write-Host "   Effects: $($fragment.Effect)"
        }
        foreach ($effectClass in @($fragment.EffectClasses)) {
            if ($nonDomainClasses -contains $effectClass) {
                continue
            }
            $effectFile = Find-EffectClassFile -EffectClassName $effectClass
            $handlerFile = Find-EffectHandlerFile -EffectClassName $effectClass
            if ($effectFile) {
                Write-Host "   ${effectClass}: exists ($effectFile)"
            } else {
                Write-Host "   ${effectClass}: MISSING in domain (likely needs new effect)"
            }
            if ($handlerFile) {
                Write-Host "     handler: $handlerFile"
            }
        }
        if ($fragment.Reference) {
            $referenceFile = Find-ClassFile -ClassName $fragment.Reference -RootPath "magical-vibes-card/src/main/java"
            if ($referenceFile) {
                Write-Host "   Reference card: $referenceFile"
            }
        }
    }
}

function Get-NewEffectAssessment {
    param([object[]] $OracleFragments)

    $nonDomainClasses = @("ActivatedAbility", "EquipAbility", "FlashbackAbility", "CrewAbility")
    $missing = New-Object System.Collections.Generic.List[string]
    foreach ($fragment in $OracleFragments) {
        foreach ($effectClass in @($fragment.EffectClasses)) {
            if ($nonDomainClasses -contains $effectClass) {
                continue
            }
            if (-not (Find-EffectClassFile -EffectClassName $effectClass)) {
                $missing.Add($effectClass)
            }
        }
    }

    $uniqueMissing = @($missing | Select-Object -Unique)
    if ($uniqueMissing.Count -eq 0) {
        return @{
            Status = "no"
            Reason = "All mapped effect classes already exist in magical-vibes-domain."
        }
    }

    return @{
        Status = "maybe"
        Reason = "Missing mapped classes: $($uniqueMissing -join ', '). Verify oracle mapping before creating new effects."
    }
}

function Get-PatternDocRoute {
    param(
        [object] $Card,
        [string] $OracleText
    )

    $routes = New-Object System.Collections.Generic.List[string]
    if (-not $OracleText) {
        return @()
    }

    if ($Card -and $Card.type_line -match '\bLand\b') { $routes.Add("CARD_PATTERNS_LANDS_SPELLS.md") }
    if ($OracleText -match '(?i)instant|sorcery|deals .* damage|destroy target|counter target|draw') {
        $routes.Add("CARD_PATTERNS_LANDS_SPELLS.md")
    }
    if ($Card -and $Card.type_line -match '\bCreature\b' -and $OracleText -match '(?i)enters the battlefield') {
        $routes.Add("CARD_PATTERNS_CREATURES_ETB.md")
    }
    if ($OracleText -match '(?i)whenever|at the beginning of|attacks|blocks|dies') {
        $routes.Add("CARD_PATTERNS_CREATURES_TRIGGERED.md")
    }
    if ($OracleText -match '(?i)metalcraft|morbid|during your turn|during turns other than yours|get \+|anthem|lord') {
        $routes.Add("CARD_PATTERNS_PERMANENTS_STATIC.md")
    }
    if ($Card -and $Card.type_line -match '\bArtifact\b') { $routes.Add("CARD_PATTERNS_PERMANENTS_ARTIFACTS.md") }
    if ($OracleText -match '(?i)equip|crew|planeswalker|saga|chapter|\{T\}:') {
        $routes.Add("CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md")
    }
    if ($OracleText -match '(?i)target player draws|creatures you control get|destroy target creature|counter target spell') {
        $routes.Add("CARD_COPY_PASTE_TEMPLATES.md")
    }

    return @($routes | Select-Object -Unique)
}

function Get-MinimalTestChecklist {
    param(
        [object] $Card,
        [string] $OracleText,
        [object[]] $OracleFragments,
        [object[]] $KnownMatches
    )

    $items = New-Object System.Collections.Generic.List[string]
    $items.Add("assert card structure (effects/abilities/slots) without testing Scryfall metadata")

    if (Test-IsBasicLand -Card $Card) {
        return @("skip tests (basic land)")
    }
    if (Test-IsVanillaCard -Card $Card) {
        return @("skip tests (vanilla card)")
    }

    foreach ($fragment in $OracleFragments) {
        foreach ($note in @($fragment.TestNotes)) {
            if ($note -and -not ($items -contains $note)) {
                $items.Add($note)
            }
        }
    }
    foreach ($match in $KnownMatches) {
        foreach ($note in @($match.TestNotes)) {
            if ($note -and -not ($items -contains $note)) {
                $items.Add($note)
            }
        }
    }

    if ($Card -and $Card.type_line -match '(?i)Instant|Sorcery' -and $items.Count -le 2) {
        $items.Add("cast + resolve primary effect")
        $items.Add("fizzle when target illegal before resolution (if targeted)")
    }
    if ($OracleText -match '(?i)enters the battlefield' -and $items.Count -le 2) {
        $items.Add("ETB trigger resolves with expected outcome")
    }

    return @($items | Select-Object -Unique)
}

function Write-CompositeCardSkeleton {
    param(
        [object[]] $OracleFragments,
        [string] $ClassName,
        [string] $SetCode,
        [string] $CollectorNumber,
        [string] $PackageLetter
    )

    $templated = @($OracleFragments | Where-Object { $_.ConstructorLines -and $_.Imports })
    if ($templated.Count -eq 0) {
        return $false
    }

    $imports = [ordered]@{
        "com.github.laxika.magicalvibes.cards.CardRegistration" = $true
        "com.github.laxika.magicalvibes.model.Card" = $true
        "com.github.laxika.magicalvibes.model.EffectSlot" = $true
    }
    foreach ($fragment in $templated) {
        foreach ($import in @($fragment.Imports)) {
            $imports[$import] = $true
        }
    }
    $needsActivated = $false
    foreach ($fragment in $templated) {
        if (@($fragment.EffectClasses) -contains "ActivatedAbility") {
            $needsActivated = $true
        }
    }
    if ($needsActivated -and -not $imports.Contains("com.github.laxika.magicalvibes.model.ActivatedAbility")) {
        $imports["com.github.laxika.magicalvibes.model.ActivatedAbility"] = $true
    }

    Write-Host "Confidence: composite oracle fragments ($($templated.Count) templated piece(s))"
    Write-Host ""
    Write-Host "Card skeleton:"
    Write-Host "package com.github.laxika.magicalvibes.cards.$PackageLetter;"
    Write-Host ""
    foreach ($import in $imports.Keys) {
        Write-Host "import $import;"
    }
    Write-Host ""
    Write-Host "@CardRegistration(set = `"$($SetCode.ToUpperInvariant())`", collectorNumber = `"$CollectorNumber`")"
    Write-Host "public class $ClassName extends Card {"
    Write-Host ""
    Write-Host "    public $ClassName() {"
    foreach ($fragment in $templated) {
        Write-Host "        // $($fragment.Label)"
        foreach ($line in @($fragment.ConstructorLines)) {
            Write-Host "        $line"
        }
        Write-Host ""
    }
    Write-Host "    }"
    Write-Host "}"
    return $true
}

function Write-ReferenceConstructor {
    param([string] $ReferenceClassName)

    $referenceFile = Find-ClassFile -ClassName $ReferenceClassName -RootPath "magical-vibes-card/src/main/java"
    if (-not $referenceFile) {
        Write-Host "No reference constructor available."
        return
    }

    Write-Host "From $ReferenceClassName ($referenceFile):"
    $snippet = Get-ConstructorSnippet -FilePath $referenceFile
    if ($snippet.Count -eq 0) {
        Write-Host "Could not extract constructor."
        return
    }
    $snippet | ForEach-Object { Write-Host $_ }
}

function Write-TokenSavingPlan {
    param(
        [object[]] $ClassHits,
        [object[]] $KnownMatches,
        [object[]] $OracleFragments,
        [hashtable] $NewEffectAssessment,
        [bool] $HasFastPathTemplate,
        [bool] $HasCompositeTemplate,
        [string] $OracleText,
        [object] $Card
    )

    if ($ClassHits.Count -gt 0) {
        Write-Host "Mode: REPRINT - add @CardRegistration only; do not implement logic or run card tests."
        Write-Host "Read: none (class already exists)."
        Write-Host "Skip: all docs, codebase exploration, new effects, tests."
        return
    }

    $readFiles = New-Object System.Collections.Generic.List[string]
    $references = @($KnownMatches + $OracleFragments | ForEach-Object { $_.Reference } | Where-Object { $_ })
    $bestReference = @($references | Group-Object | Sort-Object Count -Descending | Select-Object -First 1).Name
    $orderedReferences = @()
    if ($bestReference) { $orderedReferences += $bestReference }
    $orderedReferences += @($references | Where-Object { $_ -ne $bestReference } | Select-Object -Unique)
    foreach ($ref in $orderedReferences | Select-Object -First 2) {
        $cardFile = Find-ClassFile -ClassName $ref -RootPath "magical-vibes-card/src/main/java"
        $testFile = Find-ClassFile -ClassName "${ref}Test" -RootPath "magical-vibes-application/src/test/java"
        if ($cardFile) { $readFiles.Add($cardFile) }
        if ($testFile) { $readFiles.Add($testFile) }
    }

    Write-Host "Mode: NEW CARD"
    Write-Host "New effect needed: $($NewEffectAssessment.Status) - $($NewEffectAssessment.Reason)"
    if ($HasFastPathTemplate -or $HasCompositeTemplate) {
        Write-Host "Confidence: high (template available in script output)"
        Write-Host "Skip broad exploration: yes - use Fast Path / Composite skeleton below"
    } else {
        Write-Host "Confidence: medium/low - grep docs only, do not read them in full"
    }

    Write-Host ""
    Write-Host "Read only (max 3 files):"
    if ($readFiles.Count -eq 0) {
        Write-Host "- none suggested; use Oracle Effect Mapping references"
    } else {
        $readFiles | Select-Object -First 3 | ForEach-Object { Write-Host "- $_" }
    }

    Write-Host ""
    Write-Host "Do NOT:"
    Write-Host "- Read agent-docs files in full"
    Write-Host "- Run the full test suite"
    Write-Host "- Re-fetch Scryfall (already in this script output)"

    $docRoutes = Get-PatternDocRoute -Card $Card -OracleText $OracleText
    Write-Host ""
    Write-Host "Grep docs only (if template insufficient):"
    if ($docRoutes.Count -eq 0) {
        Write-Host "- agent-docs/EFFECTS_QUICK_REFERENCE.md"
        Write-Host "- agent-docs/ORACLE_TEXT_EFFECT_MAP.md"
    } else {
        foreach ($route in $docRoutes) {
            Write-Host "- agent-docs/$route"
        }
    }

    $grepTerms = @($OracleFragments + $KnownMatches | ForEach-Object {
        @($_.EffectClasses) + @($_.Effect -split '\s*\+\s*')
    } | ForEach-Object { $_ } | Where-Object { $_ } | Select-Object -Unique)
    if ($grepTerms.Count -gt 0) {
        Write-Host "Suggested grep keywords: $($grepTerms -join ', ')"
    }
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
$oracleFragments = Get-OracleFragments -OracleText $oracleText -KnownMatches $knownMatches
$newEffectAssessment = Get-NewEffectAssessment -OracleFragments $oracleFragments

Write-Section "New Effect Needed"
Write-Host "$($newEffectAssessment.Status) - $($newEffectAssessment.Reason)"

Write-Section "Oracle Effect Mapping"
Write-OracleEffectMapping -OracleFragments $oracleFragments

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
} elseif ($oracleFragments.Count -gt 0) {
    $primaryFragment = @($oracleFragments | Sort-Object {
        $lines = 0
        if ($_.ConstructorLines) { $lines = @($_.ConstructorLines).Count }
        $lines
    } -Descending | Select-Object -First 1)[0]
    Write-Host "Closest pattern: $($primaryFragment.Reference) ($($primaryFragment.Label))"
    $primaryReferenceCardFile = Find-ClassFile -ClassName $primaryFragment.Reference -RootPath "magical-vibes-card/src/main/java"
    if ($primaryReferenceCardFile) {
        Write-Host "Closest card file: $primaryReferenceCardFile"
    }
    $bestCompositeReference = @($oracleFragments | ForEach-Object { $_.Reference } |
        Group-Object | Sort-Object Count -Descending | Select-Object -First 1).Name
    if ($bestCompositeReference -and $bestCompositeReference -ne $primaryFragment.Reference) {
        Write-Host "Best composite reference: $bestCompositeReference"
    }
} else {
    Write-Host "Closest pattern: none found; inspect Effect Doc Hits/Existing Usages below."
}

$minimalTestChecklist = Get-MinimalTestChecklist -Card $card -OracleText $oracleText -OracleFragments $oracleFragments -KnownMatches $knownMatches
Write-Section "Minimal Test Checklist"
if ($minimalTestChecklist.Count -eq 0) {
    Write-Host "No checklist generated."
} else {
    foreach ($item in $minimalTestChecklist) {
        Write-Host "- $item"
    }
}

Write-Section "Fast Path Template"
$hasFastPathTemplate = $false
$hasCompositeTemplate = $false
if ($classHits.Count -gt 0) {
    Write-Host "Existing class found. Prefer adding a @CardRegistration for this printing if it is the same Oracle card."
} else {
    $hasFastPathTemplate = Write-FastPathTemplate `
        -KnownMatches $knownMatches `
        -ClassName $ClassName `
        -SetCode $SetCode `
        -CollectorNumber $CollectorNumber `
        -PackageLetter $packageLetter
    if (-not $hasFastPathTemplate) {
        $hasCompositeTemplate = Write-CompositeCardSkeleton `
            -OracleFragments $oracleFragments `
            -ClassName $ClassName `
            -SetCode $SetCode `
            -CollectorNumber $CollectorNumber `
            -PackageLetter $packageLetter
        if (-not $hasCompositeTemplate) {
            Write-Host "No composite skeleton available."
        }
    }
}

$primaryReference = $null
if ($knownMatches.Count -gt 0) {
    $primaryReference = @($knownMatches)[0].Reference
} elseif ($oracleFragments.Count -gt 0) {
    $primaryReference = @($oracleFragments | ForEach-Object { $_.Reference } |
        Group-Object | Sort-Object Count -Descending | Select-Object -First 1).Name
    if (-not $primaryReference) {
        $primaryReference = @($oracleFragments)[0].Reference
    }
}
if ($primaryReference) {
    Write-Section "Reference Constructor"
    Write-ReferenceConstructor -ReferenceClassName $primaryReference
}

Write-Section "Token-Saving Plan"
Write-TokenSavingPlan `
    -ClassHits $classHits `
    -KnownMatches $knownMatches `
    -OracleFragments $oracleFragments `
    -NewEffectAssessment $newEffectAssessment `
    -HasFastPathTemplate $hasFastPathTemplate `
    -HasCompositeTemplate $hasCompositeTemplate `
    -OracleText $oracleText `
    -Card $card

$skipBroadContext = ($hasFastPathTemplate -or $hasCompositeTemplate -or $classHits.Count -gt 0) -and -not $Full
if ($skipBroadContext) {
    Write-Section "Broad Context"
    if ($classHits.Count -gt 0) {
        Write-Host "Skipped because an existing class was found. Re-run with -Full to include Effect Doc Hits and Existing Usages."
    } elseif ($hasCompositeTemplate) {
        Write-Host "Skipped because a composite skeleton was found. Re-run with -Full to include Effect Doc Hits and Existing Usages."
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
$testClass = "com.github.laxika.magicalvibes.cards.$packageLetter.${ClassName}Test"
Write-Host "./gradlew :magical-vibes-application:test --tests `"$testClass`" -x :magical-vibes-frontend:buildAngular"
Write-Host "(fallback if frontend assets are stale: drop the -x flag)"
