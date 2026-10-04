$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

# Load the functions without downloading data or rewriting the generated page.
$tokens = $null
$parseErrors = $null
$ast = [Management.Automation.Language.Parser]::ParseFile(
    (Join-Path $PSScriptRoot 'generate-set-progress.ps1'), [ref]$tokens, [ref]$parseErrors)
if ($parseErrors.Count) { throw 'Generator has PowerShell syntax errors.' }
foreach ($definition in $ast.FindAll({ param($node)
    $node -is [Management.Automation.Language.FunctionDefinitionAst]
}, $false)) {
    Invoke-Expression $definition.Extent.Text
}

foreach ($statement in $ast.EndBlock.Statements) {
    if ($statement -is [Management.Automation.Language.AssignmentStatementAst] -and
        $statement.Left.Extent.Text -in @('$script:BaseCollectorNumberPattern', '$script:LetteredCollectorNumberPattern')) {
        Invoke-Expression $statement.Extent.Text
    }
}
$script:SetCardsCache = @{}
$userAgent = 'set-progress-test'
$RequestTimeoutSeconds = 7
$printings = @(
    [pscustomobject]@{Code='OLD'; Class='Reprint.java'},
    [pscustomobject]@{Code='OLD2'; Class='Reprint.java'},
    [pscustomobject]@{Code='NEW'; Class='Reprint.java'},
    [pscustomobject]@{Code='NEW'; Class='Upcoming.java'},
    [pscustomobject]@{Code='OLD'; Class='Released.java'}
)
if ((Get-ReleasedUniqueCardCount -Printings $printings -UnreleasedCodes @{NEW=$true}) -ne 2) {
    throw 'Released classes were not deduplicated or upcoming-only classes were counted.'
}
if ((Get-ReleasedUniqueCardCount -Printings @() -UnreleasedCodes @{}) -ne 0) {
    throw 'Empty registrations must count as zero.'
}
$fixtureDir = Join-Path ([IO.Path]::GetTempPath()) ('set-progress-' + [guid]::NewGuid())
New-Item -ItemType Directory -Path $fixtureDir | Out-Null

try {
    $fixturePath = Join-Path $fixtureDir 'tst.json'
    @'
{"data":{"cards":[
  {"number":"1","name":"Front // Back","language":"English","identifiers":{"unused":"metadata"}},
  {"number":"1","name":"Front // Back","language":"English"},
  {"number":"2","name":"Other"},
  {"number":"3","name":"Foreign","language":"French"},
  {"number":"4a","name":"Art A","language":"English"},
  {"number":"4b","name":"Art B","language":"English"},
  {"number":"5","name":"Plain","language":"English"},
  {"number":"5d","name":"Demo","language":"English","isAlternative":true},
  {"number":"6z","name":"Serialized","language":"English"},
  {"number":"10","name":"Other","language":"English"},
  {"number":"GP10","name":"Other","language":"English"}
]}}
'@ | Set-Content -LiteralPath $fixturePath -Encoding UTF8

    $cards = Get-MtgJsonSetCards -SetCode TST -RepoRoot $fixtureDir -SetCacheDir $fixtureDir -MaxAgeHours 720
    if ($cards.Count -ne 11) { throw 'Card rows were lost during loading.' }
    if ($cards[0].PSObject.Properties.Name -contains 'identifiers') {
        throw 'The card cache retained unused nested metadata.'
    }
    $implementedNames = Get-ImplementedCardNames -Printings @(
        [pscustomobject]@{Code='TST'; Number='1'},
        [pscustomobject]@{Code='TST'; Number='2'}
    ) -RepoRoot $fixtureDir -SetCacheDir $fixtureDir -MaxAgeHours 720
    if ($implementedNames -isnot [Collections.Generic.HashSet[string]] -or
        $implementedNames.Count -ne 2 -or -not $implementedNames.Contains('front // back')) {
        throw 'Implemented names lost their hash set or case-insensitive membership lookup.'
    }
    $eligible = Get-EnglishPlayableEligible -SetCode TST -Fallback 5 -PrintingFallback 10 `
        -RepoRoot $fixtureDir -SetCacheDir $fixtureDir -MaxAgeHours 720
    if ($eligible.Count -ne 5 -or $eligible.Names.Contains('Demo') -or $eligible.Names.Contains('Foreign')) {
        throw 'Card loading changed language, alternative printing, or name eligibility.'
    }
    $registered = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
    foreach ($number in @('1', '3', '4a', '99')) { [void] $registered.Add($number) }
    $progress = Get-PrintingProgress -Eligible $eligible -RegisteredNumbers $registered
    if ($progress.Total -ne 8 -or $progress.Implemented -ne 2 -or
        @(Compare-Object @('2', '4b', '5', '5d', '10', 'GP10') @($progress.MissingNumbers)).Count) {
        throw 'English printing intersection or missing numbers changed.'
    }

    @'
{"data":{"cards":[
  {"number":"GR1","name":"MED GR","language":"English"},
  {"number":"RA1","name":"MED RA","language":"English"},
  {"number":"WS1","name":"MED WS","language":"English"},
  {"number":"GP1","name":"G18 GP","language":"English"},
  {"number":"A1","name":"TD0 A","language":"English"},
  {"number":"B1","name":"TD0 B","language":"English"},
  {"number":"B1","name":"TD0 B","language":"English"},
  {"number":"GP2a","name":"Prefixed art","language":"English"},
  {"number":"GP3z","name":"Serialized","language":"English"},
  {"number":"A2","name":"Foreign","language":"French"}
]}}
'@ | Set-Content -LiteralPath (Join-Path $fixtureDir 'prefix.json') -Encoding UTF8
    $eligible = Get-EnglishPlayableEligible -SetCode PREFIX -Fallback 2 -PrintingFallback 9 `
        -RepoRoot $fixtureDir -SetCacheDir $fixtureDir -MaxAgeHours 720
    if ($eligible.Count -ne 7 -or $eligible.Names.Count -ne 7 -or
        $eligible.Names.Contains('Foreign') -or $eligible.Names.Contains('Serialized')) {
        throw 'Prefixed numbers were lost or language, suffix, or duplicate filtering changed.'
    }
    $prefixedRegistrations = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
    foreach ($number in @('GR1', 'GP1', 'B1', 'GP3z', 'A2', '99')) {
        [void] $prefixedRegistrations.Add($number)
    }
    $progress = Get-PrintingProgress -Eligible $eligible -RegisteredNumbers $prefixedRegistrations
    if ($progress.Total -ne 7 -or $progress.Implemented -ne 3 -or -not $progress.CatalogAvailable -or
        @(Compare-Object @('RA1', 'WS1', 'A1', 'GP2a') @($progress.MissingNumbers)).Count) {
        throw 'Prefixed printing counts, registration matching, or missing numbers are incorrect.'
    }

    '{"data":{"cards":[]}}' | Set-Content -LiteralPath (Join-Path $fixtureDir 'empty.json') -Encoding UTF8
    $emptyRegistrations = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
    foreach ($attempt in 1..2) {
        $cards = Get-MtgJsonSetCards -SetCode EMPTY -RepoRoot $fixtureDir -SetCacheDir $fixtureDir -MaxAgeHours 720
        if ($null -eq $cards -or $cards.Count -ne 0) {
            throw 'An empty card catalog was treated as unavailable on disk or in memory.'
        }
        $eligible = Get-EnglishPlayableEligible -SetCode EMPTY -Fallback 0 -PrintingFallback 1 `
            -RepoRoot $fixtureDir -SetCacheDir $fixtureDir -MaxAgeHours 720
        $progress = Get-PrintingProgress -Eligible $eligible -RegisteredNumbers $emptyRegistrations
        if (-not $progress.CatalogAvailable -or $progress.Total -ne 0 -or $progress.Implemented -ne 0) {
            throw 'An empty card catalog fell back to the published token count.'
        }
    }

    $script:downloadCalls = 0
    function Invoke-WebRequest {
        param($Uri, [switch]$UseBasicParsing, $UserAgent, $TimeoutSec)
        if ($TimeoutSec -ne 7) { throw 'Download did not receive the configured timeout.' }
        $script:downloadCalls++
        throw 'Simulated unavailable endpoint.'
    }
    $cards = Get-MtgJsonSetCards -SetCode NONE -RepoRoot $fixtureDir -SetCacheDir $fixtureDir `
        -MaxAgeHours 720 -AllowFetch
    $eligible = Get-EnglishPlayableEligible -SetCode NONE -Fallback 8 -PrintingFallback 12 `
        -RepoRoot $fixtureDir -SetCacheDir $fixtureDir -MaxAgeHours 720 -AllowFetch
    $progress = Get-PrintingProgress -Eligible $eligible -RegisteredNumbers $registered
    if ($null -ne $cards -or $script:downloadCalls -ne 1 -or $progress.CatalogAvailable -or $progress.Total -ne 12) {
        throw 'Failed download was retried or did not fall back to published size.'
    }
    '{' | Set-Content -LiteralPath (Join-Path $fixtureDir 'broken.json') -Encoding UTF8
    $cards = Get-MtgJsonSetCards -SetCode BROKEN -RepoRoot $fixtureDir -SetCacheDir $fixtureDir `
        -MaxAgeHours 720 -Refresh -AllowFetch
    $cards = Get-MtgJsonSetCards -SetCode BROKEN -RepoRoot $fixtureDir -SetCacheDir $fixtureDir `
        -MaxAgeHours 720 -Refresh -AllowFetch
    if ($null -ne $cards -or $script:downloadCalls -ne 2) {
        throw 'An unreadable cache caused a failed download to be retried.'
    }
    Write-Host 'PASS: released-card counting, compact card loading, prefixed collector numbers, language and art filtering, DFC deduplication, missing numbers, empty catalogs, download timeout and failure fallback.'
} finally {
    # Only remove the exact fixture files created by this test.
    Remove-Item -LiteralPath (Join-Path $fixtureDir 'tst.json') -ErrorAction SilentlyContinue
    Remove-Item -LiteralPath (Join-Path $fixtureDir 'prefix.json') -ErrorAction SilentlyContinue
    Remove-Item -LiteralPath (Join-Path $fixtureDir 'empty.json') -ErrorAction SilentlyContinue
    Remove-Item -LiteralPath (Join-Path $fixtureDir 'broken.json') -ErrorAction SilentlyContinue
    Remove-Item -LiteralPath $fixtureDir
}
