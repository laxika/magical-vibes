# Examples:
#   .\scripts\review-cards.ps1 LRW
#   .\scripts\review-cards.ps1 MOR -Runner claude
#   .\scripts\review-cards.ps1 LRW -Runner grok
#   .\scripts\review-cards.ps1 LRW -Runner codex
#   .\scripts\review-cards.ps1 LRW -Runner muse
#   .\scripts\review-cards.ps1 LRW -Runner codex -Effort xhigh
#   .\scripts\review-cards.ps1 LRW -Runner codex -Fast
#   .\scripts\review-cards.ps1 LRW -ListOnly
#   .\scripts\review-cards.ps1 sos 1 5  # Optional range; skips unimplemented cards.
#
# The muse runner drives the claude CLI against Meta's Muse endpoint and needs
# $env:MODEL_API_KEY to be set first:
#   $env:MODEL_API_KEY = "<your key>"

param(
    # The set code to review cards from, e.g. "sos".
    [Parameter(Mandatory = $true, Position = 0)]
    [Alias("SetId")]
    [ValidatePattern('^[a-zA-Z0-9]+$')]
    [string] $SetCode,

    # Optional lower bound (inclusive, using the numeric part of collector numbers).
    [Parameter(Position = 1)]
    [ValidateRange(0, [int]::MaxValue)]
    [int] $From,

    # Optional upper bound (inclusive, using the numeric part of collector numbers).
    [Parameter(Position = 2)]
    [ValidateRange(0, [int]::MaxValue)]
    [int] $To,

    # Which CLI to run: "claude" (default), "grok" (Cursor agent with Grok),
    # "codex", or "muse" (the claude CLI pointed at Meta's Muse endpoint).
    [ValidateSet("claude", "grok", "codex", "muse")]
    [string] $Runner = "claude",

    # Model override. Defaults depend on -Runner:
    #   claude -> claude-opus-4-8
    #   grok   -> cursor-grok-4.5-high
    #   codex  -> gpt-5.6-luna
    #   muse   -> muse-spark-1.2-contributor
    [string] $Model,

    # Reasoning effort for the codex runner. Defaults to "xhigh" and is ignored
    # by the other runners.
    [ValidateSet("low", "medium", "high", "xhigh", "max")]
    [string] $Effort,

    # Enable fast mode for the codex runner. Ignored by the other runners.
    [switch] $Fast,

    # Print the discovered collector numbers without warming the cache or running reviews.
    [switch] $ListOnly
)

$ErrorActionPreference = "Stop"

if ($PSBoundParameters.ContainsKey("From") -and $PSBoundParameters.ContainsKey("To") -and $From -gt $To) {
    Write-Error "From ($From) must be less than or equal to To ($To)."
    exit 1
}

$SetCode = $SetCode.ToUpperInvariant()
$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$cardRoot = Join-Path $repositoryRoot "magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards"
if (-not (Test-Path -LiteralPath $cardRoot -PathType Container)) {
    throw "Card source directory not found: $cardRoot"
}

# Read every registration, including reprints declared on cards first implemented in other sets.
# Use the same annotation format as generate-set-progress.ps1.
$registrationPattern = [regex] '@CardRegistration\(\s*set\s*=\s*"([^"]+)"\s*,\s*collectorNumber\s*=\s*"([^"]+)"\s*\)'
$collectorNumbers = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::Ordinal)
foreach ($file in Get-ChildItem -LiteralPath $cardRoot -Recurse -Filter *.java -File) {
    $source = [System.IO.File]::ReadAllText($file.FullName)
    foreach ($registration in $registrationPattern.Matches($source)) {
        if ($registration.Groups[1].Value -ieq $SetCode) {
            [void] $collectorNumbers.Add($registration.Groups[2].Value)
        }
    }
}

# Keep collector numbers as strings (e.g. "14b"), but sort/filter by their leading number.
$hasFrom = $PSBoundParameters.ContainsKey("From")
$hasTo = $PSBoundParameters.ContainsKey("To")
$cardIds = @($collectorNumbers | ForEach-Object {
    $leadingNumber = [regex]::Match($_, '^\d+')
    $numericPart = if ($leadingNumber.Success) { [long] $leadingNumber.Value } else { 0 }
    if ((-not $hasFrom -or $numericPart -ge $From) -and (-not $hasTo -or $numericPart -le $To)) {
        [pscustomobject] @{ CardId = $_; NumericPart = $numericPart }
    }
} | Sort-Object NumericPart, CardId | Select-Object -ExpandProperty CardId)

if ($cardIds.Count -eq 0) {
    throw "No implemented cards found for $SetCode with the requested collector number bounds."
}

if ($ListOnly) {
    $cardIds
    return
}

if (-not $PSBoundParameters.ContainsKey("Model") -or [string]::IsNullOrWhiteSpace($Model)) {
    $Model = switch ($Runner) {
        "grok" { "cursor-grok-4.5-high" }
        "codex" { "gpt-5.6-luna" }
        "muse" { "muse-spark-1.2-contributor" }
        default { "claude-opus-4-8" }
    }
}

if (-not $PSBoundParameters.ContainsKey("Effort") -or [string]::IsNullOrWhiteSpace($Effort)) {
    $Effort = "xhigh"
}

if ($Runner -ne "codex" -and $PSBoundParameters.ContainsKey("Effort")) {
    Write-Warning "-Effort is only supported by the codex runner; ignoring it for $Runner."
}

if ($Runner -ne "codex" -and $Fast) {
    Write-Warning "-Fast is only supported by the codex runner; ignoring it for $Runner."
}

$cliName = switch ($Runner) {
    "grok" { "agent" }
    "codex" { "codex" }
    default { "claude" }
}
if (-not (Get-Command $cliName -ErrorAction SilentlyContinue)) {
    Write-Error "The '$cliName' CLI was not found on PATH."
    exit 1
}

if ($Runner -eq "muse") {
    if ([string]::IsNullOrWhiteSpace($env:MODEL_API_KEY)) {
        Write-Error "The muse runner needs an API key. Set it first with: `$env:MODEL_API_KEY = `"<your key>`""
        exit 1
    }

    $env:ANTHROPIC_BASE_URL = "https://api.meta.ai"
    $env:ANTHROPIC_AUTH_TOKEN = $env:MODEL_API_KEY
    $env:ANTHROPIC_MODEL = $Model
    $env:ANTHROPIC_DEFAULT_OPUS_MODEL = $Model
    $env:ANTHROPIC_DEFAULT_SONNET_MODEL = $Model
    $env:ANTHROPIC_DEFAULT_HAIKU_MODEL = $Model
    $env:CLAUDE_CODE_SUBAGENT_MODEL = $Model
    $env:ENABLE_TOOL_SEARCH = "true"
}

$systemPrompt = "Do not ask clarifying questions, wait for confirmation, or present multiple options. Simply choose the recommended/best approach and review the card immediately. Be thorough in the review. Implementation is read-only: do not edit card classes, effects, predicates, or docs - only delete a stale pass result file when required. Review every existing test for the card under review for rules accuracy and correctness. Ensure every test class for the card under review has @CardUsed({...}) listing every concrete card class it constructs, including support cards; add or correct class- and method-level annotations as needed. This annotation maintenance is required and is an exception to the read-only and test-modification restrictions. Make sure that only one set's cards are used in the test (preferably the set where the tested card is from). If it is not possible, then the test could use other ones as well, but we should try to stick to a limited number of sets if possible. Fix or otherwise modify an existing test only when it is wrong. Also inspect the current test harness for higher-level helpers. When an existing helper can replace multiple lines in the card's tests without changing their behavior or coverage, refactor those tests to use it instead of duplicating lower-level steps; this cleanup is an exception to the preceding restriction. Tests are encouraged: when oracle coverage is below 100% or you spot realistic edge cases, ADD focused harness tests. Always look up cards used for testing. Verify their real mana costs and other parameters using the MCP. Whenever possible, use real cards for testing. New failing tests that confirm a bug are good - leave them and report FAIL. Write scripts/result/{SET}/{collectorNumber}.txt only when there are real issues; on a clean pass delete any stale result file and write nothing."

$total = $cardIds.Count
Write-Host "Found $total implemented card(s) for $SetCode."

if ($Runner -eq "codex") {
    Write-Host "Runner: $Runner  Model: $Model  Effort: $Effort"
    if ($Fast) {
        Write-Host "Fast mode: enabled"
    }
}
else {
    Write-Host "Runner: $Runner  Model: $Model"
}

$cardInfoLauncher = Join-Path $PSScriptRoot "..\mcp\card-info\start.ps1"
Write-Host "Warming Card Info cache for $($SetCode.ToUpperInvariant())..."
& $cardInfoLauncher cache-set $SetCode | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Error "Could not populate the Card Info cache for $SetCode."
    exit $LASTEXITCODE
}

$reviewJob = {
    param(
        [string] $JobRunner,
        [string] $JobModel,
        [string] $JobEffort,
        [string] $JobRepositoryRoot,
        [string] $JobSetCode,
        [string] $JobCardId,
        [string] $JobSystemPrompt,
        [bool] $JobFast
    )

    try {
        Set-Location -LiteralPath $JobRepositoryRoot
        $prompt = "/review-card $JobSetCode $JobCardId"
        $commandOutput = @()

        if ($JobRunner -eq "grok") {
            $commandOutput = @(& agent -p --force --trust --model $JobModel "$prompt`n`n$JobSystemPrompt" 2>&1)
            $exitCode = $LASTEXITCODE
        }
        elseif ($JobRunner -eq "codex") {
            # A non-Stop EAP prevents native stderr from aborting or deadlocking
            # under Windows PowerShell 5.1. Codex output is intentionally quiet.
            $ErrorActionPreference = "Continue"
            $reasoningConfig = "model_reasoning_effort=`"$JobEffort`""
            $fastArgs = @()
            if ($JobFast) {
                $fastArgs = @("--config", 'service_tier="fast"')
            }
            & codex --search --ask-for-approval never exec --model $JobModel --config $reasoningConfig @fastArgs --cd $JobRepositoryRoot "$prompt`n`n$JobSystemPrompt" *>$null
            $exitCode = $LASTEXITCODE
        }
        else {
            $commandOutput = @(& claude --permission-mode auto --model $JobModel -p $prompt --append-system-prompt $JobSystemPrompt 2>&1)
            $exitCode = $LASTEXITCODE
        }

        [pscustomobject] @{
            CardId = $JobCardId
            ExitCode = $exitCode
            Output = @($commandOutput | ForEach-Object { $_.ToString() })
        }
    }
    catch {
        [pscustomobject] @{
            CardId = $JobCardId
            ExitCode = 1
            Output = @($_.Exception.Message)
        }
    }
}

$index = 0

foreach ($cardId in $cardIds) {
    $index++
    $startedAt = Get-Date -Format "yyyy-MM-dd HH:mm"
    Write-Host ""
    Write-Host "############################################################"
    Write-Host "# [$startedAt] [$index/$total] review-card $SetCode $cardId"
    Write-Host "############################################################"

    $result = & $reviewJob $Runner $Model $Effort $repositoryRoot $SetCode $cardId $systemPrompt $Fast.IsPresent

    foreach ($line in @($result.Output)) {
        Write-Host $line
    }

    if ($result.ExitCode -ne 0) {
        Write-Error "Review failed for $SetCode $($result.CardId) with exit code $($result.ExitCode)."
        exit $result.ExitCode
    }
}

Write-Host ""
Write-Host "Done. Reviewed $total implemented card(s) from $SetCode."
Write-Host "Findings (if any) are under scripts/result/<SET>/<collectorNumber>.txt"
