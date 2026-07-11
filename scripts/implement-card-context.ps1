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

    # Optional reference card class names. For each, the script prints the
    # constructor body inline and the test file path so you don't have to Read
    # the whole file. Pick these from agent-docs (CARD_PATTERN_INDEX.md, etc.).
    [string[]] $Reference,

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

function Write-ReferenceConstructor {
    param([string] $ReferenceClassName)

    $referenceFile = Find-ClassFile -ClassName $ReferenceClassName -RootPath "magical-vibes-card/src/main/java"
    if (-not $referenceFile) {
        Write-Host "${ReferenceClassName}: no card class found."
        return
    }

    Write-Host "-- $ReferenceClassName ($referenceFile)"
    $snippet = Get-ConstructorSnippet -FilePath $referenceFile
    if ($snippet.Count -eq 0) {
        Write-Host "   Could not extract constructor."
    } else {
        $snippet | ForEach-Object { Write-Host "   $_" }
    }

    $testFile = Find-ClassFile -ClassName "${ReferenceClassName}Test" -RootPath "magical-vibes-application/src/test/java"
    if ($testFile) {
        Write-Host "   Test: $testFile"
    }
}

function Invoke-CardContext {
    param(
        [Parameter(Mandatory = $true)]
        [string] $SetCode,

        [Parameter(Mandatory = $true)]
        [string] $CollectorNumber,

        [string] $ClassName,

        [string[]] $Reference,

        [switch] $SkipScryfall
    )

    $card = $null

    Write-Section "Scryfall"
    if ($SkipScryfall) {
        Write-Host "Skipped by -SkipScryfall."
    } else {
        try {
            $card = Get-ScryfallCard -SetCode $SetCode -CollectorNumber $CollectorNumber
            if (-not $ClassName) {
                $ClassName = ConvertTo-ClassName -CardName $card.name
            }
            Write-ScryfallSummary -Card $card
        } catch {
            Write-Host "Lookup failed: $($_.Exception.Message)"
        }
    }

    if (-not $ClassName) {
        Write-Host "ClassName could not be derived for $SetCode $CollectorNumber. Re-run with the optional -ClassName argument (single card only), or allow Scryfall lookup to succeed."
        return
    }

    Write-Section "Reprint Check"
    $classHits = Invoke-RepoSearch -Pattern "class\s+$ClassName\s+" -Paths @("magical-vibes-card/src/main/java") -MaxResults 20
    if ($classHits.Count -gt 0) {
        Write-Host "EXISTING CLASS FOUND - this is a reprint."
        $classHits | ForEach-Object { Write-Host $_ }
        Write-Host "Action: add a @CardRegistration(set, collectorNumber) annotation for this printing only."
        Write-Host "Do NOT implement logic or write/run tests."
    } else {
        Write-Host "No existing class: $ClassName (new card)."
    }

    Write-Section "Tests Guidance"
    if (Test-IsBasicLand -Card $card) {
        Write-Host "Skip tests (basic land)."
    } elseif (Test-IsVanillaCard -Card $card) {
        Write-Host "Skip tests (vanilla card, no engine behavior)."
    } elseif ($classHits.Count -gt 0) {
        Write-Host "Skip tests (reprint of an existing class)."
    } else {
        Write-Host "Write focused card behavior tests (effects/abilities/targeting only; never Scryfall metadata)."
    }

    if ($Reference -and $Reference.Count -gt 0) {
        Write-Section "Reference Constructors"
        foreach ($ref in $Reference) {
            Write-ReferenceConstructor -ReferenceClassName $ref
        }
    }

    $packageLetter = $ClassName.Substring(0, 1).ToLowerInvariant()

    Write-Section "Suggested Files"
    Write-Host "Card: magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/$packageLetter/$ClassName.java"
    Write-Host "Test: magical-vibes-application/src/test/java/com/github/laxika/magicalvibes/cards/$packageLetter/${ClassName}Test.java"

    Write-Section "Suggested Test Command"
    $testClass = "com.github.laxika.magicalvibes.cards.$packageLetter.${ClassName}Test"
    Write-Host "./gradlew :magical-vibes-application:test --tests `"$testClass`" -x :magical-vibes-frontend:buildAngular"
    Write-Host "(fallback if frontend assets are stale: drop the -x flag)"
}

$collectorNumbers = @($CollectorNumber | Where-Object { -not [string]::IsNullOrWhiteSpace($_) })
if ($collectorNumbers.Count -eq 0) {
    Write-Error "No collector number supplied."
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
    Invoke-CardContext -SetCode $SetCode -CollectorNumber $number -ClassName $ClassName -Reference $Reference -SkipScryfall:$SkipScryfall
}
