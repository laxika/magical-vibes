param(
    [Parameter(Mandatory = $true, Position = 0)]
    [string] $SetCode,

    # One or more collector numbers from the same set. Each is processed in turn
    # and gets its own full context block. E.g. `... SOS 1 2 3 4`.
    [Parameter(Mandatory = $true, Position = 1, ValueFromRemainingArguments = $true)]
    [string[]] $CollectorNumber,

    # Optional explicit class name. Only honored when a single collector number
    # is supplied; with multiple cards each name is derived from Scryfall.
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

    # Select-String reports absolute paths; trim to repo-relative so the output
    # matches what rg would have printed on a machine that has it.
    $repoRoot = (Get-Location).Path + [System.IO.Path]::DirectorySeparatorChar
    return @($files |
        Select-String -Pattern $Pattern |
        ForEach-Object { "$($_.Path.Replace($repoRoot, '')):$($_.LineNumber):$($_.Line.Trim())" } |
        Select-Object -First $MaxResults)
}

function ConvertTo-ClassName {
    param([string] $CardName)

    # Drop apostrophes so "Yawgmoth's" -> "Yawgmoths" (the trailing letters stay
    # attached to the word instead of becoming a new capitalized token).
    $cleaned = $CardName -replace "['’]", ""

    # Split on any run of non-alphanumeric characters, then PascalCase: capitalize
    # the first letter of each word and keep the rest as-is. "Bone to Ash" -> "BoneToAsh".
    $words = @($cleaned -split "[^A-Za-z0-9]+" | Where-Object { $_ -ne "" })
    return (($words | ForEach-Object {
        $_.Substring(0, 1).ToUpperInvariant() + $_.Substring(1)
    }) -join "")
}

function Get-ScryfallCard {
    param(
        [string] $SetCode,
        [string] $CollectorNumber
    )

    $cardInfoLauncher = Join-Path $PSScriptRoot "..\mcp\card-info\start.ps1"
    $json = & $cardInfoLauncher get-card $SetCode $CollectorNumber

    if ($LASTEXITCODE -ne 0) {
        throw "Card Info lookup exited with code $LASTEXITCODE"
    }

    return $json | ConvertFrom-Json
}

function Test-IsMultiFaceCard {
    param([object] $Card)

    return $Card -and $Card.card_faces -and @($Card.card_faces).Count -gt 0
}

# Prints the rules-relevant fields of a card or of one of its faces; both carry
# the same field names, so a single-faced card is just its own body.
function Write-CardBody {
    param([object] $Face)

    Write-Host "Mana: $($Face.mana_cost)"
    Write-Host "Type: $($Face.type_line)"
    if ($Face.power -or $Face.toughness) {
        Write-Host "P/T: $($Face.power)/$($Face.toughness)"
    }
    Write-Host "Oracle:"
    Write-Host $Face.oracle_text
}

function Write-ScryfallSummary {
    param([object] $Card)

    Write-Host "Name: $($Card.name)"
    if ($Card.keywords -and $Card.keywords.Count -gt 0) {
        Write-Host "Keywords: $($Card.keywords -join ', ')"
    }

    # A multi-face printing has no top-level oracle text or mana cost - the rules
    # text lives entirely on the faces.
    if (Test-IsMultiFaceCard -Card $Card) {
        foreach ($face in $Card.card_faces) {
            Write-Host ""
            Write-Host "Face: $($face.name)"
            Write-CardBody -Face $face
        }
        return
    }

    Write-CardBody -Face $Card
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

    # A multi-face card has no top-level oracle text, which would otherwise read
    # as vanilla no matter how much rules text its faces carry.
    if (Test-IsMultiFaceCard -Card $Card) {
        return $false
    }

    $hasNoOracleText = [string]::IsNullOrWhiteSpace($Card.oracle_text)
    $hasNoKeywords = -not $Card.keywords -or $Card.keywords.Count -eq 0
    return $hasNoOracleText -and $hasNoKeywords -and -not (Test-IsBasicLand -Card $Card)
}

function Invoke-CardContext {
    param(
        [Parameter(Mandatory = $true)]
        [string] $SetCode,

        [Parameter(Mandatory = $true)]
        [string] $CollectorNumber,

        [string] $ClassName,

        [switch] $SkipScryfall
    )

    $card = $null
    $classNameSupplied = -not [string]::IsNullOrWhiteSpace($ClassName)

    Write-Section "Scryfall"
    if ($SkipScryfall) {
        Write-Host "Skipped by -SkipScryfall."
    } else {
        try {
            $card = Get-ScryfallCard -SetCode $SetCode -CollectorNumber $CollectorNumber
            if (-not $classNameSupplied) {
                $ClassName = ConvertTo-ClassName -CardName $card.name
            }
            Write-ScryfallSummary -Card $card
        } catch {
            Write-Host "Lookup failed: $($_.Exception.Message)"
        }
    }

    if ((Test-IsMultiFaceCard -Card $card) -and -not $classNameSupplied) {
        Write-Section "Multi-face Card"
        Write-Host "MULTI-FACE CARD (layout: $($card.layout)) - stopping here."
        Write-Host "No class name can be derived from a two-faced card name, so the reprint check would look for a class that cannot exist. Both faces are printed above."
        Write-Host "Decide how - or whether - the engine represents this card, then re-run with -ClassName <Name> for the reprint check."
        return
    }

    if (-not $ClassName) {
        Write-Host "ClassName could not be derived for $SetCode $CollectorNumber. Re-run with the optional -ClassName argument (single card only), or allow Scryfall lookup to succeed."
        return
    }

    Write-Section "Reprint Check"
    $classHits = Invoke-RepoSearch -Pattern "class\s+$ClassName\s+" -Paths @("magical-vibes-card/src/main/java") -MaxResults 20
    if ($classHits.Count -gt 0) {
        Write-Host "EXISTING CLASS FOUND - reprint: add @CardRegistration only, no logic, no tests."
        $classHits | ForEach-Object { Write-Host $_ }
    } else {
        Write-Host "New card: $ClassName"

        # Only the exceptions are worth printing; needing tests is the norm, and
        # the paths and test command follow mechanically from the class name.
        if (Test-IsBasicLand -Card $card) {
            Write-Host "Skip tests (basic land)."
        } elseif (Test-IsVanillaCard -Card $card) {
            Write-Host "Skip tests (vanilla card, no engine behavior)."
        }
    }
}

$collectorNumbers = @($CollectorNumber | Where-Object { -not [string]::IsNullOrWhiteSpace($_) })
if ($collectorNumbers.Count -eq 0) {
    Write-Error "No collector number supplied."
    exit 1
}

# Unrecognized named parameters land in $CollectorNumber via
# ValueFromRemainingArguments, which would otherwise turn a typo into a bogus
# card lookup instead of an error.
$flagLike = @($collectorNumbers | Where-Object { $_.StartsWith("-") })
if ($flagLike.Count -gt 0) {
    Write-Error "Unknown option(s): $($flagLike -join ', '). Supported options are -ClassName and -SkipScryfall."
    exit 1
}

if ($ClassName -and $collectorNumbers.Count -gt 1) {
    Write-Error "-ClassName is only supported with a single collector number; with multiple cards each name is derived from Scryfall."
    exit 1
}

$multiple = $collectorNumbers.Count -gt 1
foreach ($number in $collectorNumbers) {
    if ($multiple) {
        Write-Host ""
        Write-Host "############################################################"
        Write-Host "# CARD: $SetCode $number"
        Write-Host "############################################################"
    }
    Invoke-CardContext -SetCode $SetCode -CollectorNumber $number -ClassName $ClassName -SkipScryfall:$SkipScryfall
}
