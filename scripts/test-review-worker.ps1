param()
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'review-worker-functions.ps1')
$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$testParent = [System.IO.Path]::GetFullPath((Join-Path $repositoryRoot 'magical-vibes-review-server/build/worker-tests'))
$testRoot = Join-Path $testParent ([guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Force -Path $testRoot | Out-Null
$script:actualGit = (Get-Item Function:\Invoke-ReviewGit).ScriptBlock
$script:mockOutcome = 'FINDINGS'
$script:mockInvalid = $false
$script:mockProduction = $false
$script:mockTestBuildFailure = $false
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
    if ($script:mockInvalid) { [System.IO.File]::WriteAllText($OutputPath, '{invalid'); return }
    $result = @{ outcome = $script:mockOutcome; cardName = 'First Card'; findings = @(); executionError = $null }
    if ($script:mockOutcome -eq 'FINDINGS') { $result.findings = @('The trigger uses the wrong controller.') }
    if ($script:mockOutcome -eq 'ERROR') { $result.executionError = 'Oracle lookup was unavailable' }
    [System.IO.File]::WriteAllText($OutputPath, ($result | ConvertTo-Json -Depth 5))
    if ($script:mockOutcome -eq 'FINDINGS') {
        Add-Content -LiteralPath (Join-Path $Root $script:taskTestPath) -Value '// focused regression'
    }
    if ($script:mockProduction) { Add-Content -LiteralPath (Join-Path $Root 'production.txt') -Value 'unexpected edit' }
}

function Invoke-ReviewFocusedTests {
    param([string] $Root, [string[]] $Paths, [string] $LogDirectory)
    if ($script:mockTestBuildFailure) { throw 'Tests did not compile' }
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

$task = [pscustomobject] @{ id = 1; runId = 2; runName = 'Worker tests'; attemptToken = [guid]::NewGuid().ToString(); model = 'test-model'; reasoningEffort = 'high'; setCode = 'INR'; collectorNumber = '14b'; className = 'example.FirstCard'; sourcePath = 'card.java' }
try {
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
    Assert-ReviewTest ((Get-ReviewGitText $checkout @('rev-parse', 'HEAD')) -eq $before) 'A clean pass created a commit'
    Write-Host 'PASS clean review creates no commit'

    $script:mockInvalid = $true
    $result = Invoke-ReviewTask $checkout $task (Join-Path $checkout 'magical-vibes-review-server/build/task-invalid')
    Assert-ReviewTest ($result.Result.outcome -eq 'ERROR' -and -not $result.Stop) 'Invalid model output was not classified as an execution failure'
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

    $checkout = New-ReviewTestCheckout 'build-failure'
    $script:mockTestBuildFailure = $true
    $result = Invoke-ReviewTask $checkout $task (Join-Path $checkout 'magical-vibes-review-server/build/task-build')
    Assert-ReviewTest ($result.Result.outcome -eq 'FINDINGS' -and $result.Result.publicationStatus -eq 'FAILED' -and $result.Stop) 'Test build failures were published or discarded completed findings'
    $script:mockTestBuildFailure = $false
    Write-Host 'PASS test build failures preserve changes for recovery'

    $checkout = New-ReviewTestCheckout 'assertion-failure'
    $script:mockTestAssertionFailure = $true
    $result = Invoke-ReviewTask $checkout $task (Join-Path $checkout 'magical-vibes-review-server/build/task-assertion')
    Assert-ReviewTest ($result.Result.outcome -eq 'FINDINGS' -and $result.Result.publicationStatus -eq 'PUSHED' -and -not $result.Stop) 'Bug-exposing test failures were not published'
    $script:mockTestAssertionFailure = $false
    Write-Host 'PASS bug-exposing failing tests publish with descriptions only'

    $pendingPath = Join-Path $testRoot 'pending.json'
    [System.IO.File]::WriteAllText($pendingPath, (@{serverUrl='http://review.test'; taskId=1; result=@{outcome='PASS'}; stop=$false} | ConvertTo-Json -Depth 5))
    $script:uploadFails = $true
    function Invoke-RestMethod { param($Uri, $Method, $ContentType, $Body, $TimeoutSec); if ($script:uploadFails) { throw 'Simulated network failure' } }
    $caught = $false
    try { Send-ReviewPendingResult 'http://review.test' $pendingPath } catch { $caught = $true }
    Assert-ReviewTest ($caught -and (Test-Path -LiteralPath $pendingPath)) 'A failed upload lost the saved result'
    $script:uploadFails = $false
    Send-ReviewPendingResult 'http://review.test' $pendingPath
    Assert-ReviewTest (-not (Test-Path -LiteralPath $pendingPath)) 'An acknowledged result was not cleared'
    Write-Host 'PASS upload retries retain results until acknowledged'
}
finally {
    $resolvedTestRoot = [System.IO.Path]::GetFullPath($testRoot)
    if (-not $resolvedTestRoot.StartsWith($testParent + [System.IO.Path]::DirectorySeparatorChar, [System.StringComparison]::OrdinalIgnoreCase)) { throw 'Refusing to remove a test directory outside the worker test workspace' }
    Remove-Item -LiteralPath $resolvedTestRoot -Recurse -Force
}
