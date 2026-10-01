<#
.SYNOPSIS
    Generates a single self-contained HTML page reporting card implementation progress per set.

.DESCRIPTION
    Scans the @CardRegistration annotations in magical-vibes-card, joins them against MTGJSON's
    SetList, and writes one static HTML file. The file is rewritten from scratch on every run, so
    deleting it and re-running restores it completely.

    Set progress counts distinct English collector numbers in MTGJSON set card data, including
    alternate-art and foil printings. It accepts digits with an optional a-d art letter, so
    serialized suffixes such as WHO's 552z are excluded. When card data is unavailable, the
    denominator falls back to MTGJSON's totalSetSize (or baseSetSize if necessary). The
    denominator also includes any registered printings absent from the card data so completion
    cannot exceed 100%.

    The Missing column counts eligible cards in the set whose name has no implemented printing
    anywhere yet — entirely new cards, not unimplemented reprints of cards the engine already has.

    Everything the page needs is inlined except web fonts and the Keyrune set-symbol font, which
    load from CDNs.

.PARAMETER OutputPath
    Where to write the page. Defaults to scripts/stats/index.html - named index.html so that
    `aws s3 sync scripts/stats/ s3://<bucket>/` serves it as the bucket's root document.

.PARAMETER SetListPath
    Use a local MTGJSON SetList.json instead of downloading one. For offline runs.

.PARAMETER SkipCardNameCatalog
    Skip the Scryfall lookup that supplies the "cards in Magic" denominator. The page still
    renders; the unique-cards tile just loses its share and meter.

.PARAMETER CachePath
    Where the downloaded MTGJSON set list is cached between runs. Defaults to
    scripts/.cache/SetList.json.

.PARAMETER CacheMaxAgeHours
    How long a cached set list stays usable. Defaults to 720 hours (30 days); pass -RefreshCache
    after a new set releases to pick it up sooner.

.PARAMETER RefreshCache
    Download the set list even if the cache is still fresh.

.EXAMPLE
    ./scripts/generate-set-progress.ps1

.EXAMPLE
    ./scripts/generate-set-progress.ps1 -OutputPath C:\tmp\progress.html

.EXAMPLE
    ./scripts/generate-set-progress.ps1 -RefreshCache
#>
[CmdletBinding()]
param(
    [string] $OutputPath,
    [string] $SetListPath,
    [switch] $SkipCardNameCatalog,
    [string] $CachePath,
    [int] $CacheMaxAgeHours = 720,
    [switch] $RefreshCache
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$repoRoot = Split-Path -Parent $PSScriptRoot
$cardRoot = Join-Path $repoRoot "magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards"

if (-not $OutputPath) {
    $OutputPath = Join-Path $PSScriptRoot "stats/index.html"
}

if (-not $CachePath) {
    $CachePath = Join-Path $PSScriptRoot ".cache/SetList.json"
}

$setCacheDir = Join-Path $PSScriptRoot ".cache/sets"
$uniqueCardCountCachePath = Join-Path $PSScriptRoot ".cache/UniqueCardCount.json"
$userAgent = "magical-vibes-set-progress/1.0"

# Same lettered art range the card browser keeps (Homelands 10a/10b, Fallen Empires a-d).
$script:BaseCollectorNumberPattern = [regex]'^\d+[a-d]?$'
$script:LetteredCollectorNumberPattern = [regex]'^(\d+)[a-d]$'
# Avoid re-reading the same MTGJSON set document when resolving names and set sizes.
$script:SetCardsCache = @{}

function Read-ImplementedPrintings {
    <#
        .SYNOPSIS
            Returns per-set printing counts, (set, number) pairs, and implemented totals.

        Card classes live in single-letter directories under cards/; the infrastructure types
        (CardSet, CardScanner, ...) sit alongside them and carry no registrations, so only the
        letter directories are scanned. Printings is the flat list of registered (set, collector
        number) pairs used later to resolve implemented card names via MTGJSON.
    #>
    param([string] $CardRoot)

    if (-not (Test-Path -LiteralPath $CardRoot)) {
        throw "Card source directory not found: $CardRoot"
    }

    $pattern = [regex] '@CardRegistration\(\s*set\s*=\s*"([^"]+)"\s*,\s*collectorNumber\s*=\s*"([^"]+)"\s*\)'

    $setCounts = @{}
    $printings = [System.Collections.Generic.List[object]]::new()
    $uniqueCards = 0
    $totalPrintings = 0
    $faceOnlyClasses = 0

    $files = Get-ChildItem -LiteralPath $CardRoot -Recurse -Filter *.java -File |
        Where-Object { $_.Directory.Name -match '^[a-z]$' }

    foreach ($file in $files) {
        $source = [System.IO.File]::ReadAllText($file.FullName)
        # Not $matches: that name is an automatic variable that the regex operators overwrite.
        $registrations = $pattern.Matches($source)

        if ($registrations.Count -eq 0) {
            # Back faces of transforming cards: real card classes with no printing of their own.
            $faceOnlyClasses++
            continue
        }

        $uniqueCards++
        $totalPrintings += $registrations.Count

        foreach ($registration in $registrations) {
            $code = $registration.Groups[1].Value.ToUpperInvariant()
            $number = $registration.Groups[2].Value
            if ($setCounts.ContainsKey($code)) {
                $setCounts[$code]++
            } else {
                $setCounts[$code] = 1
            }
            $printings.Add([pscustomobject]@{
                Code   = $code
                Number = $number
            })
        }
    }

    if ($totalPrintings -eq 0) {
        throw "No @CardRegistration annotations found under $CardRoot."
    }

    return [pscustomobject]@{
        SetCounts        = $setCounts
        Printings        = $printings
        TotalPrintings   = $totalPrintings
        UniqueCards      = $uniqueCards
        FaceOnlyClasses  = $faceOnlyClasses
        ScannedFiles     = @($files).Count
    }
}

function Read-SupportedSetCodes {
    <#
        .SYNOPSIS
            The set codes the engine can actually build decks from, per the CardSet enum.
    #>
    param([string] $CardRoot)

    $enumPath = Join-Path $CardRoot "CardSet.java"
    if (-not (Test-Path -LiteralPath $enumPath)) {
        Write-Warning "CardSet.java not found; no set will be flagged as engine-supported."
        return @()
    }

    $source = [System.IO.File]::ReadAllText($enumPath)
    $codes = [regex]::Matches($source, 'SET_[A-Z0-9]+\("([^"]+)"\)') |
        ForEach-Object { $_.Groups[1].Value.ToUpperInvariant() }

    return @($codes)
}

function Get-MtgJsonSetList {
    <#
        .SYNOPSIS
            The MTGJSON SetList, from an explicit local file, the on-disk cache, or the network.

        The set list only really moves when a new set releases, so a cached body younger than
        $MaxAgeHours is reused rather than pulling 11 MB again. A stale cache is still kept as a
        fallback: if the download fails the run continues on the old set list rather than dying,
        since a slightly outdated denominator is far better than no page at all.
    #>
    param(
        [string] $LocalPath,
        [string] $CachePath,
        [int] $MaxAgeHours,
        [switch] $Refresh
    )

    if ($LocalPath) {
        Write-Host "Reading MTGJSON set list from $LocalPath"
        return @(([System.IO.File]::ReadAllText($LocalPath) | ConvertFrom-Json).data)
    }

    $cacheAge = $null
    if (Test-Path -LiteralPath $CachePath) {
        $cacheAge = (Get-Date) - (Get-Item -LiteralPath $CachePath).LastWriteTime
    }

    if ($null -ne $cacheAge -and -not $Refresh -and $cacheAge.TotalHours -lt $MaxAgeHours) {
        Write-Host ("Using cached set list from {0} ({1:N1} days old)." -f $CachePath, $cacheAge.TotalDays)
        return @(([System.IO.File]::ReadAllText($CachePath) | ConvertFrom-Json).data)
    }

    $url = "https://mtgjson.com/api/v5/SetList.json"
    # An 11 MB body over a flaky link drops often enough that one dropped connection
    # should not cost the whole run.
    for ($attempt = 1; $attempt -le 3; $attempt++) {
        try {
            Write-Host "Downloading set list from $url (attempt $attempt of 3) ..."
            $response = Invoke-WebRequest -Uri $url -UseBasicParsing -UserAgent $userAgent
            $raw = [System.Text.Encoding]::UTF8.GetString($response.RawContentStream.ToArray())
            $parsed = $raw | ConvertFrom-Json

            # Written only after the body parses, so the cache never holds a truncated
            # or error-page response that would poison every later run.
            $cacheDir = Split-Path -Parent $CachePath
            if ($cacheDir -and -not (Test-Path -LiteralPath $cacheDir)) {
                New-Item -ItemType Directory -Path $cacheDir -Force | Out-Null
            }
            [System.IO.File]::WriteAllText($CachePath, $raw, [System.Text.UTF8Encoding]::new($false))
            Write-Host "Cached set list at $CachePath"

            return @($parsed.data)
        } catch {
            Write-Warning "Download failed: $($_.Exception.Message)"
            if ($attempt -lt 3) {
                Start-Sleep -Seconds (2 * $attempt)
                continue
            }
            if ($null -ne $cacheAge) {
                Write-Warning ("Falling back to the stale cached set list ({0:N1} days old)." -f $cacheAge.TotalDays)
                return @(([System.IO.File]::ReadAllText($CachePath) | ConvertFrom-Json).data)
            }
            throw
        }
    }
}

function Get-UniqueCardCount {
    <#
        .SYNOPSIS
            How many distinct paper, MTGO, or Arena cards exist in Magic, excluding un-cards and Vanguard. Best-effort.

        Uses the search endpoint rather than the card-name catalog so that the un-set exclusion
        matches the one applied to the set list. The two endpoints are not interchangeable: the
        catalog counts card *names* including funny and extra cards, so subtracting one from the
        other does not reconcile.
    #>
    param([string] $CachePath)

    $query = "-is:funny -t:vanguard (game:paper OR game:mtgo OR game:arena)"
    $url = "https://api.scryfall.com/cards/search?q=$([uri]::EscapeDataString($query))&unique=cards"
    try {
        Write-Host "Fetching unique card count from Scryfall ..."
        $result = Invoke-RestMethod -Uri $url -UserAgent $userAgent -Headers @{ Accept = "application/json" }
        $count = [int] $result.total_cards
        if ($count -le 0) {
            throw "Scryfall returned no unique card count."
        }
        $cacheDir = Split-Path -Parent $CachePath
        if (-not (Test-Path -LiteralPath $cacheDir)) {
            New-Item -ItemType Directory -Path $cacheDir -Force | Out-Null
        }
        $cache = @{ query = $query; total_cards = $count; fetched = (Get-Date).ToUniversalTime().ToString("o") }
        [System.IO.File]::WriteAllText($CachePath, ($cache | ConvertTo-Json -Compress),
            [System.Text.UTF8Encoding]::new($false))
        return $count
    } catch {
        Write-Warning "Could not read the Scryfall card count: $($_.Exception.Message)"
        if (Test-Path -LiteralPath $CachePath) {
            try {
                $cached = [System.IO.File]::ReadAllText($CachePath) | ConvertFrom-Json
                if ($cached.query -eq $query -and [int] $cached.total_cards -gt 0) {
                    Write-Warning "Using cached Scryfall unique card count from $($cached.fetched)."
                    return [int] $cached.total_cards
                }
            } catch {
                Write-Warning "Could not read cached Scryfall card count: $($_.Exception.Message)"
            }
        }
        return 0
    }
}

function Read-MtgJsonSetCardsFromFile {
    <#
        .SYNOPSIS
            Card array from a cached MTGJSON set document, or $null if unreadable.
    #>
    param([string] $Path)

    if (-not (Test-Path -LiteralPath $Path)) {
        return $null
    }

    try {
        $parsed = [System.IO.File]::ReadAllText($Path) | ConvertFrom-Json
        if ($null -eq $parsed.data -or $null -eq $parsed.data.cards) {
            return $null
        }
        return @($parsed.data.cards)
    } catch {
        Write-Warning "Could not parse MTGJSON set file ${Path}: $($_.Exception.Message)"
        return $null
    }
}

function Get-MtgJsonSetCards {
    <#
        .SYNOPSIS
            MTGJSON cards for one set: local card-data cache, scripts/.cache/sets, or a fetch.

        Prefers magical-vibes-card-data's mtgjson-*.json when present (already downloaded by the
        engine). Otherwise reuses scripts/.cache/sets/{code}.json, and when -AllowFetch is set
        downloads from MTGJSON once so supported sets still refine correctly on a cold machine.
    #>
    param(
        [string] $SetCode,
        [string] $RepoRoot,
        [string] $SetCacheDir,
        [int] $MaxAgeHours,
        [switch] $Refresh,
        [switch] $AllowFetch
    )

    $lower = $SetCode.ToLowerInvariant()
    if ($script:SetCardsCache.ContainsKey($lower)) {
        return $script:SetCardsCache[$lower]
    }

    $localPath = Join-Path $RepoRoot "magical-vibes-card-data/card-data-cache/mtgjson-$lower.json"
    $cards = Read-MtgJsonSetCardsFromFile -Path $localPath
    if ($null -ne $cards) {
        $script:SetCardsCache[$lower] = $cards
        return $cards
    }

    $cachePath = Join-Path $SetCacheDir "$lower.json"
    $cacheAge = $null
    if (Test-Path -LiteralPath $cachePath) {
        $cacheAge = (Get-Date) - (Get-Item -LiteralPath $cachePath).LastWriteTime
    }

    if ($null -ne $cacheAge -and -not $Refresh -and $cacheAge.TotalHours -lt $MaxAgeHours) {
        $cards = Read-MtgJsonSetCardsFromFile -Path $cachePath
        if ($null -ne $cards) {
            $script:SetCardsCache[$lower] = $cards
            return $cards
        }
    }

    if (-not $AllowFetch) {
        if ($null -ne $cacheAge) {
            $cards = Read-MtgJsonSetCardsFromFile -Path $cachePath
            if ($null -ne $cards) {
                $script:SetCardsCache[$lower] = $cards
            }
            return $cards
        }
        return $null
    }

    $url = "https://mtgjson.com/api/v5/$($SetCode.ToUpperInvariant()).json"
    try {
        Write-Host "Downloading MTGJSON set $SetCode ..."
        $response = Invoke-WebRequest -Uri $url -UseBasicParsing -UserAgent $userAgent
        $raw = [System.Text.Encoding]::UTF8.GetString($response.RawContentStream.ToArray())
        $parsed = $raw | ConvertFrom-Json
        if ($null -eq $parsed.data -or $null -eq $parsed.data.cards) {
            Write-Warning "MTGJSON set $SetCode had no card list; falling back to baseSetSize."
            return $null
        }

        if (-not (Test-Path -LiteralPath $SetCacheDir)) {
            New-Item -ItemType Directory -Path $SetCacheDir -Force | Out-Null
        }
        [System.IO.File]::WriteAllText($cachePath, $raw, [System.Text.UTF8Encoding]::new($false))
        $cards = @($parsed.data.cards)
        $script:SetCardsCache[$lower] = $cards
        return $cards
    } catch {
        Write-Warning "Could not download MTGJSON set ${SetCode}: $($_.Exception.Message)"
        if ($null -ne $cacheAge) {
            $cards = Read-MtgJsonSetCardsFromFile -Path $cachePath
            if ($null -ne $cards) {
                $script:SetCardsCache[$lower] = $cards
            }
            return $cards
        }
        return $null
    }
}

function Test-IsSkippedLetteredAlternative {
    <#
        .SYNOPSIS
            True when a lettered isAlternative printing duplicates a digit-only English sibling.
    #>
    param(
        [string] $Number,
        [object] $Card,
        [System.Collections.Generic.HashSet[string]] $EnglishNumbers
    )

    $lettered = $script:LetteredCollectorNumberPattern.Match($Number)
    if (-not $lettered.Success) {
        return $false
    }

    $isAlternative = $false
    if ($Card.PSObject.Properties.Name -contains "isAlternative" -and $Card.isAlternative) {
        $isAlternative = [bool] $Card.isAlternative
    }
    $digitSibling = $lettered.Groups[1].Value
    return ($isAlternative -and $EnglishNumbers.Contains($digitSibling))
}

function Get-EnglishPlayableEligible {
    <#
        .SYNOPSIS
            English playable card names and printing count for a set, or the SetList fallback.

        Counts distinct English (or language-less) collector numbers that are digits with an
        optional a-d art letter. Numbers whose numeric part is within MTGJSON baseSetSize
        ($Fallback) form the core total. Distinct numbers matter because MTGJSON stores each
        face of a DFC as its own row sharing one collector number (DKA would otherwise read 171
        instead of 158). Lettered isAlternative printings that share a digit-only sibling are
        skipped so Portal demo-game 6d drops out while Homelands 10b (no plain 10) stays.
        Foreign-language alt-arts such as Portal's Simplified Chinese 30s are excluded by the
        language filter.

        Printings numbered above baseSetSize count only when their card name does not already
        appear among the in-base English printings — planeswalker-deck and buy-a-box exclusives
        widen the total; same-set alternate arts that reprint a base name (Godzilla, showcase,
        borderless twins with a new number) do not enter the eligible names. PrintingCount counts
        every distinct English collector number accepted by the number pattern, including those
        alternate printings. When set card data is unavailable, Count and PrintingCount fall back
        to SetList sizes and Names is empty.
    #>
    param(
        [string] $SetCode,
        [int] $Fallback,
        [int] $PrintingFallback,
        [string] $RepoRoot,
        [string] $SetCacheDir,
        [int] $MaxAgeHours,
        [switch] $Refresh,
        [switch] $AllowFetch
    )

    $emptyNames = [System.Collections.Generic.HashSet[string]]::new(
        [System.StringComparer]::OrdinalIgnoreCase)

    $cards = Get-MtgJsonSetCards -SetCode $SetCode -RepoRoot $RepoRoot -SetCacheDir $SetCacheDir `
        -MaxAgeHours $MaxAgeHours -Refresh:$Refresh -AllowFetch:$AllowFetch
    if ($null -eq $cards) {
        return [pscustomobject]@{
            Count         = $Fallback
            PrintingCount = $PrintingFallback
            Names         = $emptyNames
        }
    }

    $baseLimit = $Fallback
    $englishNumbers = [System.Collections.Generic.HashSet[string]]::new(
        [System.StringComparer]::Ordinal)
    $inBaseCandidates = [System.Collections.Generic.List[object]]::new()
    $aboveCandidates = [System.Collections.Generic.List[object]]::new()
    $inBaseNames = [System.Collections.Generic.HashSet[string]]::new(
        [System.StringComparer]::OrdinalIgnoreCase)

    foreach ($card in $cards) {
        $lang = $null
        if ($card.PSObject.Properties.Name -contains "language") {
            $lang = $card.language
        }
        if ($null -ne $lang -and $lang -ne "English") {
            continue
        }

        $number = [string] $card.number
        if (-not $script:BaseCollectorNumberPattern.IsMatch($number)) {
            continue
        }

        [void] $englishNumbers.Add($number)

        $letteredNum = $script:LetteredCollectorNumberPattern.Match($number)
        $numericPart = if ($letteredNum.Success) {
            [int] $letteredNum.Groups[1].Value
        } else {
            [int] $number
        }

        if ($numericPart -le $baseLimit) {
            $inBaseCandidates.Add($card)
            if ($card.PSObject.Properties.Name -contains "name" -and $card.name) {
                [void] $inBaseNames.Add([string] $card.name)
            }
        } else {
            $aboveCandidates.Add($card)
        }
    }

    # Unique collector numbers: DFC front/back share a number and must count once.
    $eligibleNumbers = [System.Collections.Generic.HashSet[string]]::new(
        [System.StringComparer]::Ordinal)
    $numberNames = @{}

    foreach ($card in $inBaseCandidates) {
        $number = [string] $card.number
        if (Test-IsSkippedLetteredAlternative -Number $number -Card $card -EnglishNumbers $englishNumbers) {
            continue
        }
        [void] $eligibleNumbers.Add($number)
        if (-not $numberNames.ContainsKey($number) -and
            $card.PSObject.Properties.Name -contains "name" -and $card.name) {
            $numberNames[$number] = [string] $card.name
        }
    }

    foreach ($card in $aboveCandidates) {
        $name = $null
        if ($card.PSObject.Properties.Name -contains "name") {
            $name = [string] $card.name
        }
        if ($name -and $inBaseNames.Contains($name)) {
            continue
        }

        $number = [string] $card.number
        if (Test-IsSkippedLetteredAlternative -Number $number -Card $card -EnglishNumbers $englishNumbers) {
            continue
        }
        [void] $eligibleNumbers.Add($number)
        if (-not $numberNames.ContainsKey($number) -and $name) {
            $numberNames[$number] = $name
        }
    }

    $names = [System.Collections.Generic.HashSet[string]]::new(
        [System.StringComparer]::OrdinalIgnoreCase)
    foreach ($number in $eligibleNumbers) {
        if ($numberNames.ContainsKey($number)) {
            [void] $names.Add($numberNames[$number])
        }
    }

    return [pscustomobject]@{
        Count         = $eligibleNumbers.Count
        PrintingCount = $englishNumbers.Count
        Names         = $names
    }
}

function Get-ImplementedCardNames {
    <#
        .SYNOPSIS
            Card names that already have at least one implemented printing, resolved via MTGJSON.

        Groups registrations by set, loads each set once, and maps collector numbers to the
        MTGJSON name field (DFC faces share one "Front // Back" name). Sets with registrations
        are fetched when not cached so reprint detection stays accurate.
    #>
    param(
        [System.Collections.IEnumerable] $Printings,
        [string] $RepoRoot,
        [string] $SetCacheDir,
        [int] $MaxAgeHours,
        [switch] $Refresh
    )

    $bySet = @{}
    foreach ($printing in $Printings) {
        $code = $printing.Code
        if (-not $bySet.ContainsKey($code)) {
            $bySet[$code] = [System.Collections.Generic.HashSet[string]]::new(
                [System.StringComparer]::Ordinal)
        }
        [void] $bySet[$code].Add([string] $printing.Number)
    }

    $names = [System.Collections.Generic.HashSet[string]]::new(
        [System.StringComparer]::OrdinalIgnoreCase)
    $unresolved = 0

    foreach ($code in ($bySet.Keys | Sort-Object)) {
        $cards = Get-MtgJsonSetCards -SetCode $code -RepoRoot $RepoRoot -SetCacheDir $SetCacheDir `
            -MaxAgeHours $MaxAgeHours -Refresh:$Refresh -AllowFetch
        if ($null -eq $cards) {
            $unresolved += $bySet[$code].Count
            continue
        }

        $wanted = $bySet[$code]
        $found = [System.Collections.Generic.HashSet[string]]::new(
            [System.StringComparer]::Ordinal)
        foreach ($card in $cards) {
            $number = [string] $card.number
            if (-not $wanted.Contains($number)) {
                continue
            }
            if ($card.PSObject.Properties.Name -contains "name" -and $card.name) {
                [void] $names.Add([string] $card.name)
                [void] $found.Add($number)
            }
        }
        $unresolved += ($wanted.Count - $found.Count)
    }

    if ($unresolved -gt 0) {
        Write-Warning ("Could not resolve card names for {0} implemented printing(s); those cards will not reduce the Missing column." -f $unresolved)
    }

    return $names
}

Write-Host "Scanning card sources in $cardRoot ..."
$implemented = Read-ImplementedPrintings -CardRoot $cardRoot
Write-Host ("Found {0} printings across {1} card classes ({2} sets)." -f `
    $implemented.TotalPrintings, $implemented.UniqueCards, $implemented.SetCounts.Count)

$supportedCodes = Read-SupportedSetCodes -CardRoot $cardRoot
$supportedLookup = @{}
foreach ($code in $supportedCodes) { $supportedLookup[$code] = $true }

$setList = Get-MtgJsonSetList -LocalPath $SetListPath -CachePath $CachePath `
    -MaxAgeHours $CacheMaxAgeHours -Refresh:$RefreshCache
Write-Host "Set list contains $($setList.Count) sets."

Write-Host "Resolving implemented card names via MTGJSON ..."
$implementedNames = Get-ImplementedCardNames -Printings $implemented.Printings `
    -RepoRoot $repoRoot -SetCacheDir $setCacheDir -MaxAgeHours $CacheMaxAgeHours `
    -Refresh:$RefreshCache
Write-Host ("Resolved {0} implemented card names." -f $implementedNames.Count)

$uniqueCardNamesInMagic = 0
if (-not $SkipCardNameCatalog) {
    $uniqueCardNamesInMagic = Get-UniqueCardCount -CachePath $uniqueCardCountCachePath
}

# Set types holding nothing this engine would implement. "memorabilia" already covers the
# art-card series and the oversized-card sets; "funny" covers the Un-sets and the other
# silver-border/acorn products. Vanguard cards are also not regular deck cards. Sets matching
# these are left out of the page entirely.
$nonPlayableTypes = @("token", "memorabilia", "minigame", "promo", "treasure_chest", "funny", "vanguard")

# The List is a rotating bucket of reprints inserted into other products rather than a set in
# its own right, and at 5,075 cards it would dominate any total that counted it. ULST is the
# Unfinity foil edition of the same thing (already "funny", listed here for the record).
#
# FBB/FWB/4BB/BCHR are the foreign black- and white-bordered printings of Revised, Fourth
# Edition and Chronicles, and REN/RIN/PSAL/PS11 are the European reprint and partwork series
# (Renaissance, Rinascimento, Salvat). Every card in them is already counted under the set it
# reprints, so leaving them in would double-count those cards in the totals.
#
# PAST and PSDG contain cards created for standalone digital games. Arena- and MTGO-only sets
# remain tracked.
$nonPlayableCodes = @(
    "PLST", "ULST",
    "FBB", "FWB", "4BB", "BCHR",
    "REN", "RIN", "PSAL", "PS11",
    "PAST", "PSDG"
)

$sets = [System.Collections.Generic.List[object]]::new()
$seenCodes = @{}

foreach ($set in $setList) {
    $code = $set.code.ToUpperInvariant()
    $seenCodes[$code] = $true

    $implementedCount = 0
    if ($implemented.SetCounts.ContainsKey($code)) {
        $implementedCount = $implemented.SetCounts[$code]
    }

    # Excluded unless something is actually implemented there: dropping a set that holds
    # implemented printings would leave those printings in the numerator with no row and no
    # denominator to sit in, so keep it visible and say so instead.
    if (($nonPlayableTypes -contains $set.type) -or ($nonPlayableCodes -contains $code)) {
        if ($implementedCount -eq 0) {
            continue
        }
        Write-Warning "Set '$code' is excluded from progress totals but has $implementedCount implemented printings; keeping it."
    }

    $printingFallback = if ($set.PSObject.Properties.Name -contains "totalSetSize") {
        [int] $set.totalSetSize
    } else {
        [int] $set.baseSetSize
    }
    $eligible = Get-EnglishPlayableEligible -SetCode $code -Fallback ([int] $set.baseSetSize) `
        -PrintingFallback $printingFallback `
        -RepoRoot $repoRoot -SetCacheDir $setCacheDir -MaxAgeHours $CacheMaxAgeHours `
        -Refresh:$RefreshCache -AllowFetch
    $baseSize = $eligible.Count
    # Registrations with non-numeric collector numbers can still push the denominator up.
    $total = [Math]::Max($eligible.PrintingCount, $implementedCount)
    if ($total -le 0) { $total = 0 }

    # Missing = eligible cards with no implemented printing of that name anywhere (entirely new
    # work). When set card data was unavailable Names is empty; fall back to unimplemented
    # printings so the column is still populated.
    if ($eligible.Names.Count -gt 0) {
        $missing = 0
        foreach ($name in $eligible.Names) {
            if (-not $implementedNames.Contains($name)) {
                $missing++
            }
        }
    } else {
        $missing = [Math]::Max(0, $total - $implementedCount)
    }

    $keyrune = ""
    if ($set.PSObject.Properties.Name -contains "keyruneCode" -and $set.keyruneCode) {
        $keyrune = $set.keyruneCode.ToLowerInvariant()
    }

    $sets.Add([pscustomobject][ordered]@{
        code       = $code
        name       = $set.name
        released   = $set.releaseDate
        type       = $set.type
        keyrune    = $keyrune
        base       = $baseSize
        total      = $total
        impl       = $implementedCount
        missing    = $missing
        supported  = [bool] $supportedLookup.ContainsKey($code)
        onlineOnly = [bool] $set.isOnlineOnly
    })
}

# A registration pointing at a set MTGJSON does not know about would silently vanish from the
# page, so surface it instead.
foreach ($code in $implemented.SetCounts.Keys) {
    if (-not $seenCodes.ContainsKey($code)) {
        Write-Warning "Set code '$code' has $($implemented.SetCounts[$code]) implemented printings but is not in the MTGJSON set list."
        $count = $implemented.SetCounts[$code]
        $sets.Add([pscustomobject][ordered]@{
            code       = $code
            name       = $code
            released   = ""
            type       = "unknown"
            keyrune    = ""
            base       = $count
            total      = $count
            impl       = $count
            missing    = 0
            supported  = [bool] $supportedLookup.ContainsKey($code)
            onlineOnly = $false
        })
    }
}

$supportedSets = @($sets | Where-Object { $_.supported })
$supportedTotal = ($supportedSets | Measure-Object -Property total -Sum).Sum
$supportedImpl = ($supportedSets | Measure-Object -Property impl -Sum).Sum
if (-not $supportedTotal) { $supportedTotal = 0 }
if (-not $supportedImpl) { $supportedImpl = 0 }

$missingInSupported = ($supportedSets | Measure-Object -Property missing -Sum).Sum
if (-not $missingInSupported) { $missingInSupported = 0 }

# $sets now holds exactly the tracked universe, so the headline figures and the table always
# describe the same thing.
$completeSets = @($sets | Where-Object { $_.total -gt 0 -and $_.impl -ge $_.total }).Count
$startedSets = @($sets | Where-Object { $_.impl -gt 0 }).Count

$printingsInMagic = ($sets | Measure-Object -Property total -Sum).Sum
if (-not $printingsInMagic) { $printingsInMagic = 0 }
$missingPrintings = [Math]::Max(0, $printingsInMagic - $implemented.TotalPrintings)
$missingUniqueCards = if ($uniqueCardNamesInMagic -gt 0) {
    [Math]::Max(0, $uniqueCardNamesInMagic - $implemented.UniqueCards)
} else {
    $null
}

$payload = [ordered]@{
    generated = (Get-Date).ToUniversalTime().ToString("yyyy-MM-dd HH:mm 'UTC'")
    totals    = [ordered]@{
        uniqueCards        = $implemented.UniqueCards
        printings          = $implemented.TotalPrintings
        faceOnlyClasses    = $implemented.FaceOnlyClasses
        supportedSetCount  = $supportedSets.Count
        supportedTotal     = $supportedTotal
        supportedImpl      = $supportedImpl
        missingInSupported = $missingInSupported
        startedSets        = $startedSets
        completeSets       = $completeSets
        setsInMagic        = $sets.Count
        uniqueCardsInMagic = $uniqueCardNamesInMagic
        printingsInMagic   = $printingsInMagic
        missingPrintings   = $missingPrintings
        missingUniqueCards = $missingUniqueCards
    }
    sets      = @($sets)
}

$json = $payload | ConvertTo-Json -Depth 6 -Compress
# Keep the payload from being able to close its own <script> element.
$json = $json.Replace("<", "\u003c")

$template = @'
<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Magical Vibes &mdash; Card Implementation Progress</title>
<meta name="description" content="Implementation progress of Magic sets in the Magical Vibes engine.">
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Cinzel:wght@400;600;700&family=Crimson+Text:wght@400;600;700&display=swap">
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/keyrune@3.19.0/css/keyrune.min.css">
<style>
:root {
  --color-gold: #c8a951;
  --color-parchment: #f5e6c8;
  --color-parchment-dark: #ecd9b0;
  --color-dark-wood: #2b1b17;
  --color-darkest-wood: #1a1210;
  --color-border-tan: #c4a882;
  --color-text-brown: #5c3a2e;
  /* The app's #8b7060 lands at 3.3:1 on parchment; darkened here to clear AA at 13px. */
  --color-text-muted: #6b5344;
  --color-dark-border: #3b2314;
  --color-red-primary: #8b1a1a;
  --color-green-primary: #2d7a50;
  --meter-track: rgba(92, 58, 46, 0.16);
  /* Both fills clear 3:1 against the resolved track (#d5c09b); the app's lighter
     gold and green sit at 2.5:1 and 2.9:1 and read as washed out on parchment. */
  --meter-fill: #7d5512;
  --meter-fill-done: #256843;
  --gradient-parchment: linear-gradient(180deg, #f5e6c8 0%, #ecd9b0 100%);
  --gradient-dark-wood: linear-gradient(180deg, #2b1b17 0%, #1a1210 100%);
  --font-display: 'Cinzel', Georgia, serif;
  --font-body: 'Crimson Text', Georgia, 'Times New Roman', serif;
}

* { box-sizing: border-box; }

body {
  margin: 0;
  padding: 16px;
  font-family: var(--font-body);
  color: #d4c0a0;
  background:
    repeating-linear-gradient(0deg, transparent, transparent 52px, rgba(0,0,0,0.06) 52px, rgba(0,0,0,0.06) 54px),
    repeating-linear-gradient(90deg, transparent, transparent 78px, rgba(0,0,0,0.04) 78px, rgba(0,0,0,0.04) 80px),
    linear-gradient(180deg, var(--color-darkest-wood) 0%, var(--color-dark-wood) 30%, var(--color-darkest-wood) 60%, #0d0a07 100%);
  background-attachment: fixed;
  min-height: 100vh;
}

.shell { max-width: 1180px; margin: 0 auto; display: flex; flex-direction: column; gap: 14px; }

.banner {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
  background: var(--gradient-dark-wood);
  padding: 16px 20px;
  border-radius: 4px;
  border: 2px solid var(--color-dark-border);
  border-bottom-color: var(--color-red-primary);
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.4);
}

.banner h1 {
  margin: 0;
  color: var(--color-gold);
  font-family: var(--font-display);
  font-size: 21px;
  font-weight: 700;
  letter-spacing: 2px;
  text-transform: uppercase;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);
}

.banner p { margin: 4px 0 0; color: var(--color-border-tan); font-size: 14px; }

.hero { margin-left: auto; text-align: right; }

.hero-value {
  font-family: var(--font-body);
  font-size: 52px;
  font-weight: 700;
  line-height: 1;
  color: var(--color-gold);
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
}

.hero-label {
  margin-top: 4px;
  color: var(--color-border-tan);
  font-family: var(--font-display);
  font-size: 11px;
  letter-spacing: 1.4px;
  text-transform: uppercase;
}

.tiles {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(172px, 1fr));
  gap: 10px;
}

.tile {
  background: var(--gradient-parchment);
  border: 2px solid var(--color-border-tan);
  border-radius: 4px;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.3);
  padding: 12px 14px;
}

.tile-label {
  color: var(--color-text-brown);
  font-family: var(--font-display);
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 1.2px;
  text-transform: uppercase;
}

.tile-figure {
  margin-top: 5px;
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  justify-content: space-between;
  gap: 2px 8px;
}

.tile-value {
  color: #3d2418;
  font-size: 28px;
  font-weight: 700;
  line-height: 1.05;
}

/* No chip behind this: at 13px on a tinted pill it read as a disabled badge. The
   meter below carries the colour, so the figure itself stays in plain ink. */
.tile-pct {
  flex-shrink: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--color-text-brown);
  white-space: nowrap;
}

/* Same component as the set rows use, so a tile and a row read the same way. */
.tile-meter { margin-top: 8px; }

.tile-note { margin-top: 7px; color: var(--color-text-muted); font-size: 13px; }

.panel {
  background: var(--gradient-parchment);
  border: 2px solid var(--color-border-tan);
  border-radius: 4px;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.3);
  padding: 12px;
}

.controls { display: flex; flex-wrap: wrap; gap: 10px; align-items: center; }

.type-filters {
  display: flex;
  flex-wrap: wrap;
  gap: 6px 12px;
  align-items: center;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid rgba(196, 168, 130, 0.55);
}

.type-filters-label {
  font-family: var(--font-display);
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 1.1px;
  text-transform: uppercase;
  color: var(--color-text-brown);
  margin-right: 2px;
}

.type-filter {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  color: var(--color-text-brown);
  cursor: pointer;
  user-select: none;
}

.type-filter input {
  width: 14px;
  height: 14px;
  margin: 0;
  accent-color: var(--meter-fill);
  cursor: pointer;
}

.type-filter-action {
  padding: 2px 8px;
  font-family: var(--font-display);
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 0.8px;
  text-transform: uppercase;
  color: var(--color-text-brown);
  background: transparent;
  border: 1px solid var(--color-border-tan);
  border-radius: 3px;
  cursor: pointer;
}

.type-filter-action:hover { background: rgba(92, 58, 46, 0.09); }

.search {
  flex: 1;
  min-width: 190px;
  padding: 7px 10px;
  font-family: var(--font-body);
  font-size: 15px;
  color: var(--color-text-brown);
  background: #fbf3e2;
  border: 1px solid var(--color-border-tan);
  border-radius: 3px;
}

.search:focus { outline: 2px solid var(--meter-fill); outline-offset: 1px; }

.segmented { display: flex; gap: 3px; }

.chip {
  padding: 6px 12px;
  font-family: var(--font-display);
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.8px;
  text-transform: uppercase;
  color: var(--color-text-brown);
  background: transparent;
  border: 1px solid var(--color-border-tan);
  border-radius: 3px;
  cursor: pointer;
}

.chip:hover { background: rgba(92, 58, 46, 0.09); }

.chip[aria-pressed="true"] {
  background: linear-gradient(180deg, #4a2c17 0%, #3b2314 100%);
  border-color: #7a5a40;
  color: var(--color-parchment);
}

.sort-label {
  font-family: var(--font-display);
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 1.1px;
  text-transform: uppercase;
  color: var(--color-text-brown);
}

select {
  padding: 6px 8px;
  font-family: var(--font-body);
  font-size: 14px;
  color: var(--color-text-brown);
  background: #fbf3e2;
  border: 1px solid var(--color-border-tan);
  border-radius: 3px;
}

table { width: 100%; border-collapse: collapse; }

caption {
  caption-side: top;
  text-align: left;
  padding: 0 2px 8px;
  color: var(--color-text-muted);
  font-size: 14px;
}

thead th {
  padding: 6px 8px;
  text-align: left;
  font-family: var(--font-display);
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 1.1px;
  text-transform: uppercase;
  color: var(--color-text-brown);
  border-bottom: 1px solid var(--color-border-tan);
  white-space: nowrap;
}

tbody td {
  padding: 7px 8px;
  border-bottom: 1px solid rgba(196, 168, 130, 0.5);
  color: var(--color-text-brown);
  font-size: 15px;
  vertical-align: middle;
}

tbody tr:hover td { background: rgba(92, 58, 46, 0.06); }
tbody tr:last-child td { border-bottom: none; }

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

.num { text-align: right; font-variant-numeric: tabular-nums; white-space: nowrap; }
.col-sym { width: 30px; text-align: center; }
.col-code { width: 62px; }
.col-type { width: 128px; }
.col-rel { width: 96px; }
.col-meter { width: 168px; }
.col-count { width: 104px; }
.col-missing { width: 72px; }
.col-pct { width: 58px; }

.ss { font-size: 19px; color: #4a3323; }

.code {
  display: inline-block;
  padding: 1px 6px;
  font-family: var(--font-display);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.6px;
  color: #43291c;
  background: rgba(92, 58, 46, 0.12);
  border-radius: 3px;
}

.set-name { font-weight: 600; color: #3d2418; }

.badge {
  margin-left: 6px;
  padding: 1px 5px;
  font-family: var(--font-display);
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 0.7px;
  text-transform: uppercase;
  color: #f0e4cb;
  background: var(--color-green-primary);
  border-radius: 2px;
  vertical-align: 1px;
}

.type, .rel { color: var(--color-text-muted); font-size: 13px; }
.rel { font-variant-numeric: tabular-nums; }

.meter { height: 8px; background: var(--meter-track); border-radius: 2px; overflow: hidden; }

.meter-fill {
  height: 100%;
  background: var(--meter-fill);
  border-radius: 0 4px 4px 0;
  min-width: 2px;
}

.meter-fill.done { background: var(--meter-fill-done); border-radius: 2px; }
.meter-fill.empty { display: none; }

.pct { font-weight: 700; color: #3d2418; }
.pct.zero { font-weight: 400; color: var(--color-text-muted); }

.empty-state { padding: 22px 8px; text-align: center; color: var(--color-text-muted); font-size: 15px; }

footer { padding: 4px 2px 12px; color: #8b7060; font-size: 13px; line-height: 1.6; }
footer strong { color: var(--color-border-tan); font-weight: 600; }

@media (max-width: 860px) {
  .col-type, .col-rel { display: none; }
  .hero { margin-left: 0; text-align: left; }
}
</style>
</head>
<body>
<div class="shell">

  <header class="banner">
    <div>
      <h1>Magical Vibes</h1>
      <p>Card implementation progress across every Magic set</p>
    </div>
    <div class="hero">
      <div class="hero-value" id="hero-value">&mdash;</div>
      <div class="hero-label">of supported sets implemented</div>
    </div>
  </header>

  <section class="tiles" id="tiles"></section>

  <section class="panel">
    <div class="controls">
      <input type="search" class="search" id="search" placeholder="Search by set name or code&hellip;" aria-label="Search sets">
      <div class="segmented" role="group" aria-label="Filter sets">
        <button type="button" class="chip" data-filter="all" aria-pressed="true">All sets</button>
        <button type="button" class="chip" data-filter="started" aria-pressed="false">In progress</button>
        <button type="button" class="chip" data-filter="full" aria-pressed="false">Fully supported</button>
      </div>
      <label class="sort-label" for="sort">Sort</label>
      <select id="sort">
        <option value="impl">Most implemented</option>
        <option value="pct">Highest completion</option>
        <option value="released">Newest first</option>
        <option value="oldest">Oldest first</option>
        <option value="name">Name (A&ndash;Z)</option>
        <option value="size">Largest set</option>
        <option value="missing">Most missing</option>
      </select>
    </div>
    <div class="type-filters" id="type-filters" role="group" aria-label="Filter by set type"></div>
  </section>

  <section class="panel">
    <table>
      <caption id="table-caption"></caption>
      <thead>
        <tr>
          <th class="col-sym" scope="col"><span class="sr-only">Symbol</span></th>
          <th class="col-code" scope="col">Code</th>
          <th scope="col">Set</th>
          <th class="col-type" scope="col">Type</th>
          <th class="col-rel" scope="col">Released</th>
          <th class="col-meter" scope="col">Progress</th>
          <th class="col-count num" scope="col">Printings</th>
          <th class="col-missing num" scope="col">Missing</th>
          <th class="col-pct num" scope="col">%</th>
        </tr>
      </thead>
      <tbody id="rows"></tbody>
    </table>
    <div class="empty-state" id="empty-state" hidden>No sets match these filters.</div>
  </section>

  <footer>
    <div>
      <strong>Printings</strong> counts distinct English collector numbers in MTGJSON, including
      alternate art and foil variants. Numbered suffixes a&ndash;d are included; other suffixes,
      such as serialized WHO cards, are excluded. Where set card data is unavailable, the total
      falls back to MTGJSON&rsquo;s published set size. The
      <strong>Missing</strong> column counts cards in the set with no implemented printing of
      that name anywhere yet (entirely new cards), not unimplemented reprints.
      <strong>Promos, tokens, art cards, oversized cards, memorabilia, Un-sets, Vanguard sets,
      standalone-game cards, The List and the foreign-border and European reprint series are left
      out entirely</strong> &mdash; each is either not a regular deck card or already counted
      under the set it reprints. Arena- and MTGO-only releases remain included.
    </div>
    <div id="generated"></div>
  </footer>

</div>

<script type="application/json" id="set-data">__PAYLOAD_JSON__</script>
<script>
(function () {
  "use strict";

  var DATA = JSON.parse(document.getElementById("set-data").textContent);
  var SETS = DATA.sets;
  var T = DATA.totals;

  var TYPE_LABELS = {
    core: "Core set", expansion: "Expansion", masters: "Masters", draft_innovation: "Draft innovation",
    commander: "Commander", starter: "Starter", duel_deck: "Duel deck", from_the_vault: "From the Vault",
    premium_deck: "Premium deck", masterpiece: "Masterpiece", box: "Box set", funny: "Un-set",
    promo: "Promo", token: "Token", memorabilia: "Memorabilia", minigame: "Minigame",
    planechase: "Planechase", archenemy: "Archenemy", vanguard: "Vanguard", arsenal: "Arsenal",
    spellbook: "Spellbook", alchemy: "Alchemy", treasure_chest: "Treasure chest", eternal: "Eternal",
    unknown: "Unknown"
  };

  function fmt(n) { return n.toLocaleString("en-US"); }

  function pctOf(set) {
    if (!set.total) { return 0; }
    return Math.min(100, (set.impl / set.total) * 100);
  }

  function escapeHtml(value) {
    return String(value).replace(/[&<>"]/g, function (ch) {
      return { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;" }[ch];
    });
  }

  function share(part, whole) {
    if (!whole) { return ""; }
    var pct = (part / whole) * 100;
    // Sub-1% shares round to "0%" and read as nothing implemented, so keep a decimal there.
    return (pct > 0 && pct < 1 ? pct.toFixed(1) : Math.round(pct)) + "%";
  }

  function meterHtml(pct) {
    var cls = "meter-fill" + (pct >= 99.95 ? " done" : "") + (pct <= 0 ? " empty" : "");
    return '<div class="meter tile-meter"><div class="' + cls +
      '" style="width:' + pct.toFixed(1) + '%"></div></div>';
  }

  function renderTiles() {
    var overall = T.supportedTotal ? (T.supportedImpl / T.supportedTotal) * 100 : 0;
    document.getElementById("hero-value").textContent = overall.toFixed(1) + "%";

    var missingUnique = T.missingUniqueCards != null
      ? T.missingUniqueCards
      : Math.max(0, T.uniqueCardsInMagic - T.uniqueCards);

    // Every tile is the same shape: how much of `whole` is implemented. Deliberately no
    // tile counts what is *missing* as its figure -- a near-full bar on a "missing" tile
    // reads as good news when it means the opposite.
    var tiles = [
      {
        label: "Unique cards",
        value: fmt(T.uniqueCards),
        part: T.uniqueCards,
        whole: T.uniqueCardsInMagic,
        note: T.uniqueCardsInMagic
          ? "of " + fmt(T.uniqueCardsInMagic) + " in Magic \u00b7 " + fmt(missingUnique) + " missing"
          : "distinct cards implemented"
      },
      {
        label: "Printings",
        value: fmt(T.printings),
        part: T.printings,
        whole: T.printingsInMagic,
        note: "of " + fmt(T.printingsInMagic) + " in Magic \u00b7 " +
              fmt(T.missingPrintings) + " missing"
      },
      {
        label: "Supported sets",
        value: fmt(T.supportedImpl),
        part: T.supportedImpl,
        whole: T.supportedTotal,
        note: "of " + fmt(T.supportedTotal) + " printings \u00b7 " +
              fmt(T.missingInSupported) + " new cards missing"
      },
      {
        label: "Sets covered",
        value: fmt(T.startedSets),
        part: T.startedSets,
        whole: T.setsInMagic,
        note: "of " + fmt(T.setsInMagic) + " sets \u00b7 " + fmt(T.completeSets) + " complete"
      }
    ];

    document.getElementById("tiles").innerHTML = tiles.map(function (tile) {
      var pct = tile.whole ? Math.min(100, (tile.part / tile.whole) * 100) : 0;
      return '<div class="tile">' +
        '<div class="tile-label">' + escapeHtml(tile.label) + "</div>" +
        '<div class="tile-figure">' +
          '<span class="tile-value">' + escapeHtml(tile.value) + "</span>" +
          (tile.whole ? '<span class="tile-pct">' + escapeHtml(share(tile.part, tile.whole)) + "</span>" : "") +
        "</div>" +
        (tile.whole ? meterHtml(pct) : "") +
        '<div class="tile-note">' + escapeHtml(tile.note) + "</div>" +
        "</div>";
    }).join("");
  }

  var state = { filter: "all", search: "", sort: "impl", types: {} };

  function presentTypes() {
    var seen = {};
    SETS.forEach(function (set) { seen[set.type || "unknown"] = true; });
    return Object.keys(seen).sort(function (a, b) {
      return (TYPE_LABELS[a] || a).localeCompare(TYPE_LABELS[b] || b);
    });
  }

  presentTypes().forEach(function (type) { state.types[type] = true; });

  function setAllTypes(enabled) {
    presentTypes().forEach(function (type) { state.types[type] = enabled; });
    Array.prototype.forEach.call(document.querySelectorAll("#type-filters input[type=checkbox]"), function (box) {
      box.checked = enabled;
    });
    render();
  }

  function renderTypeFilters() {
    var html = '<span class="type-filters-label">Type</span>';
    html += presentTypes().map(function (type) {
      var id = "type-" + type;
      var label = TYPE_LABELS[type] || type;
      return '<label class="type-filter" for="' + escapeHtml(id) + '">' +
        '<input type="checkbox" id="' + escapeHtml(id) + '" data-type="' + escapeHtml(type) + '"' +
        (state.types[type] ? " checked" : "") + ">" +
        escapeHtml(label) +
        "</label>";
    }).join("");
    html += '<button type="button" class="type-filter-action" id="types-all">All</button>';
    html += '<button type="button" class="type-filter-action" id="types-none">None</button>';
    document.getElementById("type-filters").innerHTML = html;

    Array.prototype.forEach.call(document.querySelectorAll("#type-filters input[type=checkbox]"), function (box) {
      box.addEventListener("change", function () {
        state.types[box.dataset.type] = box.checked;
        render();
      });
    });
    document.getElementById("types-all").addEventListener("click", function () { setAllTypes(true); });
    document.getElementById("types-none").addEventListener("click", function () { setAllTypes(false); });
  }

  function isComplete(set) {
    return set.total > 0 && set.impl >= set.total;
  }

  function matches(set) {
    // "Fully supported" requires engine set support and every counted printing registered.
    if (state.filter === "full" && !(set.supported && isComplete(set))) { return false; }
    // Started but not finished: at least one printing implemented, and the set is not complete.
    if (state.filter === "started" && (set.impl === 0 || isComplete(set))) { return false; }
    if (!state.types[set.type || "unknown"]) { return false; }

    if (state.search) {
      var needle = state.search.toLowerCase();
      if (set.name.toLowerCase().indexOf(needle) === -1 &&
          set.code.toLowerCase().indexOf(needle) === -1) {
        return false;
      }
    }
    return true;
  }

  var SORTS = {
    impl: function (a, b) { return b.impl - a.impl || pctOf(b) - pctOf(a) || a.name.localeCompare(b.name); },
    pct: function (a, b) { return pctOf(b) - pctOf(a) || b.impl - a.impl || a.name.localeCompare(b.name); },
    released: function (a, b) { return (b.released || "").localeCompare(a.released || "") || a.name.localeCompare(b.name); },
    oldest: function (a, b) { return (a.released || "9999").localeCompare(b.released || "9999") || a.name.localeCompare(b.name); },
    name: function (a, b) { return a.name.localeCompare(b.name); },
    size: function (a, b) { return b.total - a.total || a.name.localeCompare(b.name); },
    missing: function (a, b) { return b.missing - a.missing || b.total - a.total || a.name.localeCompare(b.name); }
  };

  function rowHtml(set) {
    var pct = pctOf(set);
    var fillClass = "meter-fill" + (isComplete(set) ? " done" : "") + (set.impl === 0 ? " empty" : "");
    var symbol = set.keyrune ? '<i class="ss ss-' + escapeHtml(set.keyrune) + '" aria-hidden="true"></i>' : "";
    var badge = set.supported ? '<span class="badge">Supported</span>' : "";
    var pctText = set.impl === 0 ? "0%" : (pct >= 99.95 ? "100%" : pct.toFixed(0) + "%");
    var missingClass = set.missing === 0 ? " zero" : "";

    return "<tr>" +
      '<td class="col-sym">' + symbol + "</td>" +
      '<td class="col-code"><span class="code">' + escapeHtml(set.code) + "</span></td>" +
      '<td><span class="set-name">' + escapeHtml(set.name) + "</span>" + badge + "</td>" +
      '<td class="col-type type">' + escapeHtml(TYPE_LABELS[set.type] || set.type) + "</td>" +
      '<td class="col-rel rel">' + escapeHtml(set.released || "\u2014") + "</td>" +
      '<td class="col-meter"><div class="meter"><div class="' + fillClass + '" style="width:' + pct.toFixed(1) + '%"></div></div></td>' +
      '<td class="col-count num">' + fmt(set.impl) + " / " + fmt(set.total) + "</td>" +
      '<td class="col-missing num"><span class="pct' + missingClass + '">' + fmt(set.missing) + "</span></td>" +
      '<td class="col-pct num"><span class="pct' + (set.impl === 0 ? " zero" : "") + '">' + pctText + "</span></td>" +
      "</tr>";
  }

  function render() {
    var visible = SETS.filter(matches).slice().sort(SORTS[state.sort]);

    document.getElementById("rows").innerHTML = visible.map(rowHtml).join("");
    document.getElementById("empty-state").hidden = visible.length > 0;

    var shown = visible.reduce(function (acc, set) { return acc + set.impl; }, 0);
    var missing = visible.reduce(function (acc, set) { return acc + set.missing; }, 0);
    document.getElementById("table-caption").textContent =
      fmt(visible.length) + " sets shown \u00b7 " + fmt(shown) +
      " implemented printings \u00b7 " + fmt(missing) + " new cards missing";
  }

  document.getElementById("search").addEventListener("input", function (event) {
    state.search = event.target.value.trim();
    render();
  });

  document.getElementById("sort").addEventListener("change", function (event) {
    state.sort = event.target.value;
    render();
  });

  Array.prototype.forEach.call(document.querySelectorAll(".chip"), function (chip) {
    chip.addEventListener("click", function () {
      state.filter = chip.dataset.filter;
      Array.prototype.forEach.call(document.querySelectorAll(".chip"), function (other) {
        other.setAttribute("aria-pressed", String(other === chip));
      });
      render();
    });
  });

  document.getElementById("generated").textContent =
    "Generated " + DATA.generated + " \u00b7 " + fmt(T.faceOnlyClasses) +
    " additional classes implement the back faces of transforming cards and have no printing of their own.";

  renderTypeFilters();
  renderTiles();
  render();
})();
</script>
</body>
</html>
'@

$html = $template.Replace("__PAYLOAD_JSON__", $json)

$outputDir = Split-Path -Parent $OutputPath
if ($outputDir -and -not (Test-Path -LiteralPath $outputDir)) {
    New-Item -ItemType Directory -Path $outputDir -Force | Out-Null
}

# Written whole every run, so deleting the file and re-running restores it completely.
[System.IO.File]::WriteAllText($OutputPath, $html, [System.Text.UTF8Encoding]::new($false))

$sizeKb = [Math]::Round((Get-Item -LiteralPath $OutputPath).Length / 1KB, 1)
Write-Host ""
Write-Host "Wrote $OutputPath ($sizeKb KB)"
Write-Host ("  {0} unique cards, {1} printings" -f `
    $implemented.UniqueCards, $implemented.TotalPrintings)
if ($null -ne $missingUniqueCards) {
    Write-Host ("  {0} unique cards still missing (of {1} in Magic)" -f `
        $missingUniqueCards, $uniqueCardNamesInMagic)
}
Write-Host ("  {0}/{1} printings across {2} supported sets ({3:N1}%)" -f `
    $supportedImpl, $supportedTotal, $supportedSets.Count,
    $(if ($supportedTotal) { ($supportedImpl / $supportedTotal) * 100 } else { 0 }))
Write-Host ("  {0} new cards still missing in supported sets" -f $missingInSupported)
