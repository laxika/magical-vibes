param()
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'review-worker-functions.ps1')
$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$testParent = [System.IO.Path]::GetFullPath((Join-Path $repositoryRoot 'magical-vibes-review-server/build/worker-tests'))
$testRoot = Join-Path $testParent ([guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Force -Path $testRoot | Out-Null
$script:actualGit = (Get-Item Function:\Invoke-ReviewGit).ScriptBlock
$script:actualFocusedTests = (Get-Item Function:\Invoke-ReviewFocusedTests).ScriptBlock
$script:mockOutcome = 'FINDINGS'
$script:mockInvalid = $false
$script:mockProduction = $false
$script:mockPassWithTests = $false
$script:reviewInvocations = 0
$script:mockTestAssertionFailure = $false
$script:simulatePushFailure = $false
$script:simulatePushRace = $false
$script:raceInjected = $false
$script:taskTestPath = 'magical-vibes-application/src/test/java/example/cards/a/FirstCardTest.java'

function Assert-ReviewTest {
    param([bool] $Condition, [string] $Message)
    if (-not $Condition) { throw $Message }
}

function Invoke-ReviewCodex {
    param([string] $Root, $Task, [string] $OutputPath, [string] $SchemaPath, [string] $LogPath)
    $script:reviewInvocations++
    [System.IO.File]::WriteAllText($LogPath, '{"type":"turn.completed","usage":{"input_tokens":1000,"cached_input_tokens":800,"output_tokens":200}}')
    if ($script:mockInvalid) { [System.IO.File]::WriteAllText($OutputPath, '{invalid'); return }
    $outcome = $script:mockOutcome
    $result = @{ outcome = $outcome; cardName = 'First Card'; findings = @(); executionError = $null }
    if ($outcome -eq 'FINDINGS') { $result.findings = @('The trigger uses the wrong controller.') }
    if ($outcome -eq 'ERROR') { $result.executionError = 'Oracle lookup was unavailable' }
    [System.IO.File]::WriteAllText($OutputPath, ($result | ConvertTo-Json -Depth 5))
    if ($script:mockOutcome -eq 'FINDINGS' -or $script:mockPassWithTests) {
        Add-Content -LiteralPath (Join-Path $Root $script:taskTestPath) -Value '// focused regression'
    }
    if ($script:mockProduction) { Add-Content -LiteralPath (Join-Path $Root 'production.txt') -Value 'unexpected edit' }
}

function Invoke-ReviewFocusedTests {
    param([string] $Root, [string[]] $Paths, [string] $LogDirectory)
    return $script:mockTestAssertionFailure
}

function Invoke-ReviewGit {
    param([string] $Root, [string[]] $Arguments)
    if ($Arguments[0] -eq 'push' -and $script:simulatePushFailure) { return [pscustomobject] @{ ExitCode = 1; Output = 'Simulated push failure' } }
    if ($Arguments[0] -eq 'push' -and $script:simulatePushRace -and -not $script:raceInjected) {
        $script:raceInjected = $true
        Add-Content -LiteralPath (Join-Path $script:racerRoot 'other.txt') -Value 'concurrent change'
        & $script:actualGit $script:racerRoot @('add', '--', 'other.txt') | Out-Null
        & $script:actualGit $script:racerRoot @('commit', '-m', 'Concurrent test change') | Out-Null
        $racePush = & $script:actualGit $script:racerRoot @('push', 'origin', 'main')
        Assert-ReviewTest ($racePush.ExitCode -eq 0) 'The competing local test push failed'
    }
    return & $script:actualGit $Root $Arguments
}

function New-ReviewTestCheckout {
    param([string] $Name)
    $origin = Join-Path $testRoot ($Name + '-origin.git')
    $checkout = Join-Path $testRoot $Name
    New-Item -ItemType Directory -Path $checkout | Out-Null
    Get-ReviewGitText $checkout @('init', '--bare', '--initial-branch=main', $origin) | Out-Null
    Get-ReviewGitText $checkout @('init', '--initial-branch=main') | Out-Null
    Get-ReviewGitText $checkout @('config', 'user.name', 'Review worker test') | Out-Null
    Get-ReviewGitText $checkout @('config', 'user.email', 'review-test@example.invalid') | Out-Null
    $testFile = Join-Path $checkout $script:taskTestPath
    New-Item -ItemType Directory -Force -Path (Split-Path -Parent $testFile) | Out-Null
    [System.IO.File]::WriteAllText($testFile, 'package example.cards.a; public class FirstCardTest {}')
    [System.IO.File]::WriteAllText((Join-Path $checkout '.gitignore'), "**/build/`n")
    [System.IO.File]::WriteAllText((Join-Path $checkout 'production.txt'), 'production')
    [System.IO.File]::WriteAllText((Join-Path $checkout 'other.txt'), 'other')
    New-Item -ItemType Directory -Force -Path (Join-Path $checkout 'magical-vibes-review-server') | Out-Null
    [System.IO.File]::WriteAllText((Join-Path $checkout 'magical-vibes-review-server/review.sqlite'), 'manual database snapshot')
    Get-ReviewGitText $checkout @('add', '--all') | Out-Null
    Get-ReviewGitText $checkout @('commit', '-m', 'Test baseline') | Out-Null
    Get-ReviewGitText $checkout @('remote', 'add', 'origin', $origin) | Out-Null
    Get-ReviewGitText $checkout @('push', '-u', 'origin', 'main') | Out-Null
    return $checkout
}

$task = [pscustomobject] @{ id = 1; runId = 2; runName = 'Worker tests'; attemptToken = [guid]::NewGuid().ToString(); model = 'gpt-5.6-luna'; reasoningEffort = 'high'; setCode = 'INR'; collectorNumber = '14b'; className = 'example.FirstCard'; sourcePath = 'card.java' }
try {
    $prices = Read-ReviewPricing
    $usagePath = Join-Path $testRoot 'usage.jsonl'
    [System.IO.File]::WriteAllText($usagePath, "non-JSON diagnostic`n" + '{"type":"item.completed"}' + "`n" + '{"type":"turn.completed","usage":{"input_tokens":1000,"cached_input_tokens":800,"output_tokens":200,"reasoning_output_tokens":50}}')
    $usage = Get-ReviewUsage @($usagePath) 'gpt-5.6-sol' $prices
    Assert-ReviewTest ($usage.estimatedCostUsd -eq [decimal] 0.00512) 'Cost charged cached input at the full rate or counted reasoning tokens twice'
    $usage = Get-ReviewUsage @($usagePath) 'unknown-model' $prices
    Assert-ReviewTest ($usage.inputTokens -eq 1000 -and $null -eq $usage.estimatedCostUsd) 'Unknown model pricing fabricated a cost or discarded token usage'
    $usage = Get-ReviewUsage @($usagePath, (Join-Path $testRoot 'missing.jsonl')) 'gpt-5.6-sol' $prices
    Assert-ReviewTest ($null -eq $usage.inputTokens -and $null -eq $usage.estimatedCostUsd) 'Missing follow-up usage was reported as a complete total'
    [System.IO.File]::WriteAllText($usagePath, '{"type":"turn.failed"}')
    $usage = Get-ReviewUsage @($usagePath) 'gpt-5.6-sol' $prices
    Assert-ReviewTest ($null -eq $usage.estimatedCostUsd) 'A failed turn without usage was charged as zero'
    [System.IO.File]::WriteAllText($usagePath, '{"type":"turn.completed","usage":{"input_tokens":100,"cached_input_tokens":101,"output_tokens":0}}')
    $usage = Get-ReviewUsage @($usagePath) 'gpt-5.6-sol' $prices
    Assert-ReviewTest ($null -eq $usage.inputTokens) 'Invalid cached token usage was accepted'
    [System.IO.File]::WriteAllText($usagePath, '{"type":"turn.completed","usage":{"input_tokens":0,"cached_input_tokens":0,"output_tokens":0}}')
    $usage = Get-ReviewUsage @($usagePath) 'gpt-5.6-sol' $prices
    Assert-ReviewTest ($null -ne $usage.estimatedCostUsd -and $usage.estimatedCostUsd -eq 0) 'Known zero usage was treated as unavailable'
    $customPricing = Join-Path $testRoot 'prices.json'
    [System.IO.File]::WriteAllText($customPricing, '{"models":{"custom":{"inputUsdPerMillion":2,"cachedInputUsdPerMillion":0.5,"outputUsdPerMillion":10}}}')
    $customPrices = Read-ReviewPricing $customPricing
    Assert-ReviewTest ($customPrices.custom.cachedInputUsdPerMillion -eq [decimal] 0.5) 'Custom pricing was not loaded'
    [System.IO.File]::WriteAllText($customPricing, '{"models":{"custom":{"inputUsdPerMillion":2,"outputUsdPerMillion":10}}}')
    $caught = $false
    try { Read-ReviewPricing $customPricing | Out-Null } catch { $caught = $true }
    Assert-ReviewTest $caught 'Missing cached input pricing was silently treated as zero'
    Write-Host 'PASS usage parsing handles cached tokens, custom pricing, zero usage, and unavailable estimates'

    $prompt = Get-ReviewCodexPrompt $task
    Assert-ReviewTest ($prompt -match 'Do not launch Gradle or run tests yourself' -and $prompt -match '7200-second' -and $prompt -match 'Pending compilation is not a test failure') 'The worker did not delegate slow compilation and exact-class validation away from the reviewing agent'
    Write-Host 'PASS reviewing agent delegates test execution to the worker'

    $checkout = New-ReviewTestCheckout 'focused-class'
    $script:focusedCalls = @()
    function powershell.exe {
        $script:focusedCalls += ,$args
        $global:LASTEXITCODE = 0
    }
    try { & $script:actualFocusedTests $checkout @($script:taskTestPath) $testRoot | Out-Null }
    finally { Remove-Item Function:\powershell.exe }
    Assert-ReviewTest ($script:focusedCalls.Count -eq 1) 'Validation invoked more than the reviewed class'
    $call = $script:focusedCalls[0]
    Assert-ReviewTest ($call[3] -eq 'example.cards.a.FirstCardTest' -and $call[4] -eq '-TimeoutSeconds' -and $call[5] -eq 7200) 'The focused class or two-hour timeout was not forwarded to the test runner'
    Write-Host 'PASS worker validates only the exact changed class with a two-hour timeout'

    $script:focusedXml = Join-Path $checkout 'magical-vibes-application/build/test-results/test/TEST-example.cards.a.FirstCardTest.xml'
    New-Item -ItemType Directory -Force -Path (Split-Path -Parent $script:focusedXml) | Out-Null
    function powershell.exe {
        if ($script:focusedResult -in @('assertion', 'interaction')) {
            $failure = if ($script:focusedResult -eq 'assertion') { '<failure message="Expected 19 but was 20" />' } else { '<error message="Not awaiting PermanentChosen input" />' }
            $counts = if ($script:focusedResult -eq 'assertion') { 'failures="1" errors="0"' } else { 'failures="0" errors="1"' }
            [System.IO.File]::WriteAllText($script:focusedXml, "<testsuite tests=`"1`" $counts><testcase name=`"trampleDamageTriggersDestruction`">$failure</testcase></testsuite>")
        }
        $global:LASTEXITCODE = if ($script:focusedResult -eq 'timeout') { 124 } else { 1 }
    }
    try {
        foreach ($mode in @('assertion', 'interaction')) {
            $script:focusedResult = $mode
            $failed = & $script:actualFocusedTests $checkout @($script:taskTestPath) $testRoot
            Assert-ReviewTest $failed "Focused validation rejected a compiled $mode failure"
        }
        Remove-Item -LiteralPath $script:focusedXml
        foreach ($mode in @('build', 'timeout')) {
            $script:focusedResult = $mode
            $validationWarnings = @()
            $failed = & $script:actualFocusedTests $checkout @($script:taskTestPath) $testRoot -WarningVariable validationWarnings
            $expected = if ($mode -eq 'build') { 'build or tooling failure' } else { 'two-hour' }
            Assert-ReviewTest ($failed -and ($validationWarnings -join ' ') -match $expected) "Focused validation did not warn and continue after a $mode failure"
        }
    }
    finally { Remove-Item Function:\powershell.exe }
    Write-Host 'PASS focused validation accepts assertion/interaction failures and warns on build failures/timeouts'

    $unicodePath = Join-Path $testRoot 'unicode-result.json'
    $unicodeName = 'A' + [char] 0x00e9 + 'ther Adept'
    $unicodeFinding = 'The target restriction ' + [char] 0x2014 + ' is incorrect.'
    $unicodeResult = @{outcome='FINDINGS'; cardName=$unicodeName; findings=@($unicodeFinding); executionError=$null}
    [System.IO.File]::WriteAllText($unicodePath, ($unicodeResult | ConvertTo-Json -Depth 5), [System.Text.UTF8Encoding]::new($false))
    $decoded = Read-ReviewOutput $unicodePath
    Assert-ReviewTest ($decoded.cardName -eq $unicodeName -and $decoded.findings[0] -eq $unicodeFinding) 'UTF-8 names or bug descriptions were corrupted'
    Write-Host 'PASS Unicode names and findings survive structured output'

    $checkout = New-ReviewTestCheckout 'line endings'
    Get-ReviewGitText $checkout @('config', 'core.autocrlf', 'true') | Out-Null
    Get-ReviewGitText $checkout @('config', 'core.safecrlf', 'warn') | Out-Null
    [System.IO.File]::WriteAllText((Join-Path $checkout $script:taskTestPath), "package example.cards.a; public class FirstCardTest {}`n// LF regression`n")
    $diff = Invoke-ReviewGit $checkout @('-c', 'core.quotepath=false', 'diff', '--name-only', 'HEAD')
    Assert-ReviewTest ($diff.ExitCode -eq 0 -and $diff.Output -match 'LF will be replaced by CRLF') 'The line-ending regression did not produce a Git warning'
    $changedPaths = @(Get-ReviewChangedPaths $checkout)
    Assert-ReviewTest ($changedPaths.Count -eq 1 -and $changedPaths[0] -eq $script:taskTestPath) 'Git warnings were treated as changed paths'
    $result = Invoke-ReviewTask $checkout $task (Join-Path $checkout 'magical-vibes-review-server/build/task-line-endings')
    Assert-ReviewTest ($result.Result.outcome -eq 'FINDINGS' -and $result.Result.publicationStatus -eq 'PUSHED' -and -not $result.Stop) 'Permitted card tests with line-ending warnings were not published'
    Write-Host 'PASS Git line-ending warnings do not reject permitted card tests'

    $checkout = New-ReviewTestCheckout 'publish'
    $databasePath = Join-Path $checkout 'magical-vibes-review-server/review.sqlite'
    Add-Content -LiteralPath $databasePath -Value 'uncommitted database change'
    Get-ReviewGitText $checkout @('add', '--', 'magical-vibes-review-server/review.sqlite') | Out-Null
    $result = Invoke-ReviewTask $checkout $task (Join-Path $checkout 'magical-vibes-review-server/build/task-one')
    Assert-ReviewTest ($result.Result.outcome -eq 'FINDINGS' -and $result.Result.publicationStatus -eq 'PUSHED' -and -not $result.Stop) 'Findings and test changes were not published'
    $publishedPaths = Get-ReviewGitText $checkout @('diff-tree', '--no-commit-id', '--name-only', '-r', 'HEAD')
    Assert-ReviewTest ($publishedPaths -eq $script:taskTestPath) 'The worker committed something other than its card test'
    Assert-ReviewTest ((Get-ReviewGitText $checkout @('diff', '--cached', '--name-only')) -eq 'magical-vibes-review-server/review.sqlite') 'The manually staged database was not preserved'
    Write-Host 'PASS findings published; manually staged SQLite excluded'

    $script:mockOutcome = 'PASS'
    $before = Get-ReviewGitText $checkout @('rev-parse', 'HEAD')
    $result = Invoke-ReviewTask $checkout $task (Join-Path $checkout 'magical-vibes-review-server/build/task-pass')
    Assert-ReviewTest ($result.Result.outcome -eq 'PASS' -and $result.Result.publicationStatus -eq 'NOT_REQUIRED') 'A clean pass should not publish a commit'
    Assert-ReviewTest ($result.Result.inputTokens -eq 1000 -and $result.Result.cachedInputTokens -eq 800 -and $result.Result.outputTokens -eq 200 -and $result.Result.estimatedCostUsd -eq [decimal] 0.000296) 'Token cost did not account for the cached input discount'
    Assert-ReviewTest ((Get-ReviewGitText $checkout @('rev-parse', 'HEAD')) -eq $before) 'A clean pass created a commit'
    Write-Host 'PASS clean review creates no commit'

    $script:mockInvalid = $true
    $result = Invoke-ReviewTask $checkout $task (Join-Path $checkout 'magical-vibes-review-server/build/task-invalid')
    Assert-ReviewTest ($result.Result.outcome -eq 'ERROR' -and -not $result.Stop) 'Invalid model output was not classified as an execution failure'
    Assert-ReviewTest ($result.Result.estimatedCostUsd -eq [decimal] 0.000296) 'Invalid structured output discarded the consumed token cost'
    $script:mockInvalid = $false
    Write-Host 'PASS invalid structured output becomes an execution failure'

    $script:mockOutcome = 'ERROR'
    $result = Invoke-ReviewTask $checkout $task (Join-Path $checkout 'magical-vibes-review-server/build/task-error')
    Assert-ReviewTest ($result.Result.outcome -eq 'ERROR' -and $result.Result.findings.Count -eq 0 -and $result.Result.executionError -eq 'Oracle lookup was unavailable') 'Model-reported tooling failure became a card finding'
    $script:mockOutcome = 'PASS'
    Write-Host 'PASS model execution errors remain separate from bugs'

    $script:mockProduction = $true
    $result = Invoke-ReviewTask $checkout $task (Join-Path $checkout 'magical-vibes-review-server/build/task-production')
    Assert-ReviewTest ($result.Result.outcome -eq 'ERROR' -and $result.Stop) 'Unexpected production changes were not preserved with a stopped worker'
    $script:mockProduction = $false
    Write-Host 'PASS production edits stop publication and remain in the checkout'

    $checkout = New-ReviewTestCheckout 'push-failure'
    $script:mockOutcome = 'FINDINGS'
    $script:simulatePushFailure = $true
    $result = Invoke-ReviewTask $checkout $task (Join-Path $checkout 'magical-vibes-review-server/build/task-push')
    Assert-ReviewTest ($result.Result.outcome -eq 'FINDINGS' -and $result.Result.publicationStatus -eq 'FAILED' -and $result.Stop) 'Push failure discarded completed findings'
    $script:simulatePushFailure = $false
    Write-Host 'PASS push failures preserve the review verdict and commit'

    $checkout = New-ReviewTestCheckout 'race'
    $script:racerRoot = Join-Path $testRoot 'racer'
    Get-ReviewGitText $testRoot @('clone', (Join-Path $testRoot 'race-origin.git'), $script:racerRoot) | Out-Null
    Get-ReviewGitText $script:racerRoot @('config', 'user.name', 'Review worker test') | Out-Null
    Get-ReviewGitText $script:racerRoot @('config', 'user.email', 'review-test@example.invalid') | Out-Null
    $script:simulatePushRace = $true
    $result = Invoke-ReviewTask $checkout $task (Join-Path $checkout 'magical-vibes-review-server/build/task-race')
    Assert-ReviewTest ($result.Result.publicationStatus -eq 'PUSHED' -and -not $result.Stop -and $script:raceInjected) 'A non-conflicting push race did not recover'
    $script:simulatePushRace = $false
    Write-Host 'PASS concurrent main push is fetched, rebased, and retried'

    $mockFocusedTests = (Get-Item Function:\Invoke-ReviewFocusedTests).ScriptBlock
    Set-Item Function:\Invoke-ReviewFocusedTests -Value $script:actualFocusedTests
    function powershell.exe {
        Write-Output 'Simulated compileTestJava failure'
        $global:LASTEXITCODE = 1
    }
    $buildFailureResults = @()
    try {
        foreach ($outcome in @('FINDINGS', 'PASS')) {
            $checkout = New-ReviewTestCheckout "build-failure-$outcome"
            $script:mockOutcome = $outcome
            $script:mockPassWithTests = $true
            $directory = Join-Path $checkout 'magical-vibes-review-server/build/task-build'
            $beforeInvocations = $script:reviewInvocations
            $taskOutput = @(Invoke-ReviewTask $checkout $task $directory 3>&1)
            $validationWarnings = @($taskOutput | Where-Object { $_ -is [System.Management.Automation.WarningRecord] })
            $result = $taskOutput | Where-Object { $_ -isnot [System.Management.Automation.WarningRecord] }
            Assert-ReviewTest ($result.Result.outcome -eq $outcome -and $result.Result.publicationStatus -eq 'PUSHED' -and -not $result.Stop) 'Test build failure stopped publication or changed the review verdict'
            Assert-ReviewTest ($result.Result.findings.Count -eq $(if ($outcome -eq 'FINDINGS') { 1 } else { 0 }) -and $null -eq $result.Result.executionError -and $null -eq $result.Result.publicationError) 'Build diagnostics were classified as card findings or publication errors'
            Assert-ReviewTest (($validationWarnings -join ' ') -match 'build or tooling failure' -and (Get-Content -LiteralPath (Join-Path $directory 'FirstCardTest.log') -Raw) -match 'Simulated compileTestJava failure') 'Build diagnostics or the focused validation log were lost'
            Assert-ReviewTest ($script:reviewInvocations -eq $beforeInvocations + 1 -and $result.Result.estimatedCostUsd -eq [decimal] 0.000296) 'Build failure triggered an extra review or discarded token usage'
            Assert-ReviewTest ((Get-ReviewGitText $checkout @('rev-parse', 'HEAD')) -eq (Get-ReviewGitText $checkout @('rev-parse', 'origin/main')) -and @(Get-ReviewChangedPaths $checkout).Count -eq 0) 'Build failure left unpushed or unfinished test changes'
            Assert-ReviewCheckout $checkout -Pull
            $buildFailureResults += $result
        }
    }
    finally {
        Remove-Item Function:\powershell.exe
        Set-Item Function:\Invoke-ReviewFocusedTests -Value $mockFocusedTests
        $script:mockOutcome = 'FINDINGS'
        $script:mockPassWithTests = $false
    }
    Write-Host 'PASS test build failures publish tests and allow the next claim for FINDINGS and PASS reviews'

    $checkout = New-ReviewTestCheckout 'assertion-failure'
    $script:mockTestAssertionFailure = $true
    $result = Invoke-ReviewTask $checkout $task (Join-Path $checkout 'magical-vibes-review-server/build/task-assertion')
    Assert-ReviewTest ($result.Result.outcome -eq 'FINDINGS' -and $result.Result.publicationStatus -eq 'PUSHED' -and -not $result.Stop) 'Bug-exposing test failures were not published'
    $script:mockTestAssertionFailure = $false
    Write-Host 'PASS bug-exposing failing tests publish with descriptions only'

    $checkout = New-ReviewTestCheckout 'unconfirmed-test-failure'
    $script:mockOutcome = 'PASS'
    $script:mockPassWithTests = $true
    $script:mockTestAssertionFailure = $true
    $beforeInvocations = $script:reviewInvocations
    $result = Invoke-ReviewTask $checkout $task (Join-Path $checkout 'magical-vibes-review-server/build/task-unconfirmed-test-failure')
    Assert-ReviewTest ($result.Result.outcome -eq 'PASS' -and $result.Result.findings.Count -eq 0 -and $result.Result.publicationStatus -eq 'PUSHED' -and -not $result.Stop) 'Compiled failing tests without a confirmed card bug were not published'
    Assert-ReviewTest ($null -eq $result.Result.executionError -and $null -eq $result.Result.publicationError) 'A test failure was classified as an execution or publication failure'
    Assert-ReviewTest ($script:reviewInvocations -eq $beforeInvocations + 1 -and $result.Result.inputTokens -eq 1000 -and $result.Result.estimatedCostUsd -eq [decimal] 0.000296) 'A test failure triggered another review or an extra token charge'
    Assert-ReviewTest ((Get-ReviewGitText $checkout @('rev-parse', 'HEAD')) -eq (Get-ReviewGitText $checkout @('rev-parse', 'origin/main')) -and @(Get-ReviewChangedPaths $checkout).Count -eq 0) 'Accepted failing tests were not pushed or left unfinished changes'
    $script:mockPassWithTests = $false
    $script:mockTestAssertionFailure = $false
    Write-Host 'PASS compiled failing tests publish without findings or a follow-up review'

    $pendingPath = Join-Path $testRoot 'pending.json'
    [System.IO.File]::WriteAllText($pendingPath, (@{serverUrl='http://review.test'; taskId=1; result=@{outcome='PASS'; inputTokens=1000; cachedInputTokens=800; outputTokens=200; estimatedCostUsd=[decimal] 0.000296}; stop=$false} | ConvertTo-Json -Depth 5))
    $script:uploadFails = $true
    function Invoke-RestMethod { param($Uri, $Method, $ContentType, $Body, $TimeoutSec); if ($script:uploadFails) { throw 'Simulated network failure' }; $script:uploaded = [System.Text.Encoding]::UTF8.GetString($Body) | ConvertFrom-Json }
    $caught = $false
    try { Send-ReviewPendingResult 'http://review.test' $pendingPath } catch { $caught = $true }
    Assert-ReviewTest ($caught -and (Test-Path -LiteralPath $pendingPath)) 'A failed upload lost the saved result'
    $script:uploadFails = $false
    Send-ReviewPendingResult 'http://review.test' $pendingPath
    Assert-ReviewTest (-not (Test-Path -LiteralPath $pendingPath)) 'An acknowledged result was not cleared'
    Assert-ReviewTest ($script:uploaded.estimatedCostUsd -eq [decimal] 0.000296 -and $script:uploaded.cachedInputTokens -eq 800) 'Upload retry lost saved cost or usage'
    Write-Host 'PASS upload retries retain results until acknowledged'

    foreach ($completed in $buildFailureResults) {
        [System.IO.File]::WriteAllText($pendingPath, (@{serverUrl='http://review.test'; taskId=1; result=$completed.Result; stop=$completed.Stop} | ConvertTo-Json -Depth 12))
        Send-ReviewPendingResult 'http://review.test' $pendingPath
        Assert-ReviewTest (-not (Test-Path -LiteralPath $pendingPath) -and $script:uploaded.outcome -eq $completed.Result.outcome -and $script:uploaded.findings.Count -eq $completed.Result.findings.Count -and $script:uploaded.publicationStatus -eq 'PUSHED') 'Build failure prevented upload or stopped the worker after acknowledgement'
    }
    Write-Host 'PASS reviews upload after build failures without stopping the worker'

    [System.IO.File]::WriteAllText($pendingPath, (@{serverUrl='http://review.test'; taskId=1; result=@{outcome='PASS'; publicationError='Compiler failed after validation'}; stop=$true} | ConvertTo-Json -Depth 5))
    $message = ''
    try { Send-ReviewPendingResult 'http://review.test' $pendingPath } catch { $message = $_.Exception.Message }
    Assert-ReviewTest ($message -match 'Compiler failed after validation' -and -not (Test-Path -LiteralPath $pendingPath)) 'The final worker error hid the actual validation failure or retained an acknowledged result'
    Write-Host 'PASS final worker errors include the reason for stopping'
}
finally {
    $resolvedTestRoot = [System.IO.Path]::GetFullPath($testRoot)
    if (-not $resolvedTestRoot.StartsWith($testParent + [System.IO.Path]::DirectorySeparatorChar, [System.StringComparison]::OrdinalIgnoreCase)) { throw 'Refusing to remove a test directory outside the worker test workspace' }
    Remove-Item -LiteralPath $resolvedTestRoot -Recurse -Force
}
