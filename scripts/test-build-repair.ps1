param()
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'build-repair-functions.ps1')
$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$testParent = [System.IO.Path]::GetFullPath((Join-Path $repositoryRoot 'scripts/result/build-repair-tests'))
$testRoot = Join-Path $testParent ([guid]::NewGuid().ToString('N').Substring(0, 8) + ' spaces')
New-Item -ItemType Directory -Force -Path $testRoot | Out-Null
$script:mode = 'green'
$script:calls = 0
$script:directories = @()
$script:capturedModels = @()
$script:capturedEfforts = @()
$script:raceTrace = @()

function Assert-BuildRepairTest {
    param([bool] $Condition, [string] $Message)
    if (-not $Condition) { throw $Message }
}

function Assert-BuildRepairThrows {
    param([scriptblock] $Action, [string] $Pattern)
    $message = $null
    try { & $Action | Out-Null } catch { $message = $_.Exception.Message }
    Assert-BuildRepairTest ($null -ne $message -and $message -match $Pattern) "Expected error /$Pattern/, got: $message"
}

function New-BuildRepairTestCheckout {
    param([string] $Name)
    $root = Join-Path $testRoot $Name
    $origin = Join-Path $testRoot ($Name + '-origin.git')
    New-Item -ItemType Directory -Path $root | Out-Null
    Get-BuildRepairGitText $root @('init', '--bare', '--initial-branch=main', $origin) | Out-Null
    Get-BuildRepairGitText $root @('init', '--initial-branch=main') | Out-Null
    Get-BuildRepairGitText $root @('config', 'user.name', 'Build repair test') | Out-Null
    Get-BuildRepairGitText $root @('config', 'user.email', 'build-repair@example.invalid') | Out-Null
    Get-BuildRepairGitText $root @('config', 'commit.gpgsign', 'false') | Out-Null
    Get-BuildRepairGitText $root @('config', 'core.hooksPath', (Join-Path $testRoot 'no-hooks')) | Out-Null
    Write-BuildRepairText (Join-Path $root '.gitignore') "scripts/result/`n"
    Write-BuildRepairText (Join-Path $root 'gradlew.bat') "@echo off`r`nexit /b 99`r`n"
    Write-BuildRepairText (Join-Path $root 'repair.txt') "baseline`n"
    Get-BuildRepairGitText $root @('add', '--all') | Out-Null
    Get-BuildRepairGitText $root @('commit', '-m', 'Test baseline') | Out-Null
    Get-BuildRepairGitText $root @('remote', 'add', 'origin', $origin) | Out-Null
    Get-BuildRepairGitText $root @('push', '-u', 'origin', 'main') | Out-Null
    return $root
}

function New-BuildRepairTestResult {
    param([string] $Head)
    return [pscustomobject] @{
        status = 'green'; failures_targeted = 0; failures_fixed = 0; has_remaining_failures = $false
        build_exit_code = 0; full_build_head = $Head; verified_head = $Head; pushed_head = $Head
        summary = 'Full build passed on the pushed commit.'; blocking_reason = $null
    }
}

function Invoke-BuildRepairCodex {
    param([string] $Root, [string] $Directory, [string] $Model, [string] $Effort)
    $script:calls++
    $script:directories += $Directory
    $script:capturedModels += $Model
    $script:capturedEfforts += $Effort
    Write-BuildRepairText (Join-Path $Directory 'events.jsonl') '{"type":"turn.completed"}'
    if ($script:mode -eq 'codex_failure') { throw 'Simulated Codex execution failure' }
    if ($script:mode -eq 'missing_result') { return }
    if ($script:mode -eq 'invalid_json') { Write-BuildRepairText (Join-Path $Directory 'result.json') '{broken'; return }
    $head = Get-BuildRepairGitText $Root @('rev-parse', 'HEAD')
    $result = New-BuildRepairTestResult $head
    Write-BuildRepairText (Join-Path $Directory 'build.log') "BUILD SUCCESSFUL in 1s`n"

    switch ($script:mode) {
        'partial_then_green' {
            if ($script:calls -eq 1) {
                Write-BuildRepairText (Join-Path $Root 'repair.txt') "baseline`nverified repair`n"
                Get-BuildRepairGitText $Root @('add', '--', 'repair.txt') | Out-Null
                Get-BuildRepairGitText $Root @('commit', '-m', 'Fix selected failure') | Out-Null
                Get-BuildRepairGitText $Root @('push', 'origin', 'main') | Out-Null
                $result.status = 'remaining_failures'
                $result.failures_targeted = 1; $result.failures_fixed = 1
                $result.build_exit_code = 1; $result.has_remaining_failures = $true
                $result.verified_head = Get-BuildRepairGitText $Root @('rev-parse', 'HEAD')
                $result.full_build_head = $result.verified_head
                $result.pushed_head = $result.verified_head
                $result.summary = 'One selected failure fixed and pushed; another remains.'
                Write-BuildRepairText (Join-Path $Directory 'build.log') "FAILURE: Build failed with an exception.`nBUILD FAILED in 1s`n"
            }
        }
        'no_progress' {
            $result.status = 'remaining_failures'; $result.failures_targeted = 1
            $result.build_exit_code = 1; $result.has_remaining_failures = $true
        }
        'no_commit_progress' {
            $result.status = 'remaining_failures'; $result.failures_targeted = 1; $result.failures_fixed = 1
            $result.build_exit_code = 1; $result.has_remaining_failures = $true
        }
        'over_budget' { $result.failures_targeted = 101 }
        'false_green' { $result.has_remaining_failures = $true }
        'stale_head' { $result.verified_head = 'a' * 40; $result.pushed_head = $result.verified_head }
        'different_pushed_head' { $result.pushed_head = 'a' * 40 }
        'missing_log' { Remove-Item -LiteralPath (Join-Path $Directory 'build.log') }
        'failed_green_log' { Write-BuildRepairText (Join-Path $Directory 'build.log') "BUILD FAILED in 1s`n" }
        'mixed_log' { Write-BuildRepairText (Join-Path $Directory 'build.log') "BUILD FAILED in 1s`nBUILD SUCCESSFUL in 1s`n" }
        'dirty_after' { Write-BuildRepairText (Join-Path $Root 'repair.txt') 'uncommitted repair' }
        'blocked' {
            $result.status = 'blocked'; $result.build_exit_code = $null; $result.full_build_head = $null
            $result.verified_head = $null; $result.pushed_head = $null
            $result.has_remaining_failures = $true; $result.blocking_reason = 'Dependency download unavailable'
            Write-BuildRepairText (Join-Path $Root 'repair.txt') 'preserved diagnostic work'
        }
        'push_failure' {
            Write-BuildRepairText (Join-Path $Root 'repair.txt') 'local repair only'
            Get-BuildRepairGitText $Root @('add', '--', 'repair.txt') | Out-Null
            Get-BuildRepairGitText $Root @('commit', '-m', 'Preserved unpushed repair') | Out-Null
            $result.status = 'blocked'; $result.pushed_head = $null
            $result.verified_head = Get-BuildRepairGitText $Root @('rev-parse', 'HEAD')
            $result.blocking_reason = 'Push rejected after twenty attempts'
        }
        'unconfirmed_push' {
            Write-BuildRepairText (Join-Path $Root 'repair.txt') 'unpushed repair'
            Get-BuildRepairGitText $Root @('add', '--', 'repair.txt') | Out-Null
            Get-BuildRepairGitText $Root @('commit', '-m', 'Unpushed repair') | Out-Null
            $result.verified_head = Get-BuildRepairGitText $Root @('rev-parse', 'HEAD')
            $result.pushed_head = $result.verified_head
        }
        { $_ -in @('push_race', 'stale_after_rebase', 'missing_integration_log', 'stale_integration_log') } {
            $racer = Join-Path $testRoot ($script:mode + '-racer')
            Get-BuildRepairGitText $Root @('clone', (Get-BuildRepairGitText $Root @('remote', 'get-url', 'origin')), $racer) | Out-Null
            Get-BuildRepairGitText $racer @('config', 'user.name', 'Competing test') | Out-Null
            Get-BuildRepairGitText $racer @('config', 'user.email', 'race@example.invalid') | Out-Null
            Get-BuildRepairGitText $racer @('config', 'commit.gpgsign', 'false') | Out-Null
            Get-BuildRepairGitText $racer @('config', 'core.hooksPath', (Join-Path $testRoot 'no-hooks')) | Out-Null
            Write-BuildRepairText (Join-Path $Root 'repair.txt') "local verified repair`n"
            Get-BuildRepairGitText $Root @('add', '--', 'repair.txt') | Out-Null
            Get-BuildRepairGitText $Root @('commit', '-m', 'Local selected repair') | Out-Null
            $beforeRebase = Get-BuildRepairGitText $Root @('rev-parse', 'HEAD')
            $result.full_build_head = $beforeRebase
            $script:raceTrace += 'build-before-rebase'
            Write-BuildRepairText (Join-Path $racer 'repair.txt') "concurrent behavior`n"
            Get-BuildRepairGitText $racer @('add', '--', 'repair.txt') | Out-Null
            Get-BuildRepairGitText $racer @('commit', '-m', 'Concurrent main change') | Out-Null
            Get-BuildRepairGitText $racer @('push', 'origin', 'main') | Out-Null
            Assert-BuildRepairThrows { Get-BuildRepairGitText $Root @('push', 'origin', 'main') } 'rejected'
            Assert-BuildRepairThrows { Get-BuildRepairGitText $Root @('pull', '--rebase', 'origin', 'main') } 'could not apply|conflict'
            $script:raceTrace += 'resolve-conflict'
            Write-BuildRepairText (Join-Path $Root 'repair.txt') "concurrent behavior`nlocal verified repair`n"
            Get-BuildRepairGitText $Root @('add', '--', 'repair.txt') | Out-Null
            Get-BuildRepairGitText $Root @('-c', 'core.editor=true', 'rebase', '--continue') | Out-Null
            $result.verified_head = Get-BuildRepairGitText $Root @('rev-parse', 'HEAD')
            Assert-BuildRepairTest ($result.verified_head -ne $beforeRebase) 'Rebase did not change the built commit'
            $script:raceTrace += 'focused-check-after-rebase'
            if ($script:mode -ne 'missing_integration_log') {
                $integrationHead = $result.verified_head
                if ($script:mode -eq 'stale_integration_log') { $integrationHead = $beforeRebase }
                Write-BuildRepairText (Join-Path $Directory 'rebase-validation.log') "Reviewed concurrent change and conflict resolution; selected regression checks passed.`nVerified HEAD: $integrationHead`n"
            }
            if ($script:mode -eq 'stale_after_rebase') { $result.verified_head = $beforeRebase }
            Get-BuildRepairGitText $Root @('push', 'origin', 'main') | Out-Null
            $script:raceTrace += 'push'
            $result.pushed_head = Get-BuildRepairGitText $Root @('rev-parse', 'HEAD')
            $result.failures_targeted = 1; $result.failures_fixed = 1
        }
    }
    Write-BuildRepairText (Join-Path $Directory 'result.json') ($result | ConvertTo-Json -Depth 5)
}

function Invoke-BuildRepairTestScenario {
    param([string] $Mode, [string] $ErrorPattern)
    $root = New-BuildRepairTestCheckout $Mode
    $script:mode = $Mode; $script:calls = 0; $script:directories = @()
    if ($ErrorPattern) {
        Assert-BuildRepairThrows { Invoke-BuildRepairLoop -Root $root } $ErrorPattern
        Assert-BuildRepairTest ($script:calls -eq 1) "$Mode retried a failed batch"
    } else { Invoke-BuildRepairLoop -Root $root }
    foreach ($directory in $script:directories) {
        Assert-BuildRepairTest (Test-Path -LiteralPath (Join-Path $directory 'prompt.txt')) 'Batch prompt was not preserved'
        Assert-BuildRepairTest (Test-Path -LiteralPath (Join-Path $directory 'events.jsonl')) 'Codex events were not preserved'
    }
    Write-Host "PASS $Mode"
    return $root
}

try {
    foreach ($scriptPath in @('run-build-and-fix.ps1', 'build-repair-functions.ps1', 'test-build-repair.ps1')) {
        $parseTokens = $null; $parseErrors = $null
        [System.Management.Automation.Language.Parser]::ParseFile((Join-Path $PSScriptRoot $scriptPath), [ref] $parseTokens, [ref] $parseErrors) | Out-Null
        Assert-BuildRepairTest ($parseErrors.Count -eq 0) "PowerShell syntax errors in $scriptPath"
    }
    $root = Invoke-BuildRepairTestScenario 'green'
    Assert-BuildRepairTest ($script:calls -eq 1) 'Already-green checkout did not terminate'
    Assert-BuildRepairTest ($script:capturedModels[0] -eq 'gpt-6.1-sol' -and $script:capturedEfforts[0] -eq 'high') 'Model/effort defaults changed'
    $prompt = Get-Content -LiteralPath (Join-Path $script:directories[0] 'prompt.txt') -Raw
    foreach ($pattern in @('git pull --rebase origin main', 'build --continue --rerun-tasks --console=plain', 'git push origin main', 'TWENTY push attempts', 'multiple pushes per minute', 'Do not run a full build between rebases or push retries', 'Reuse previous validation when incoming changes are independent', 'run only the necessary focused checks', 'overrides AGENTS.md', 'at most 100 distinct failures')) {
        Assert-BuildRepairTest ($prompt.Contains($pattern)) "Prompt is missing a required instruction: $pattern"
    }
    foreach ($task in @(':magical-vibes-frontend:buildAngular', ':magical-vibes-frontend:testAngular', ':magical-vibes-application:copyFrontend', ':magical-vibes-ai:test')) {
        Assert-BuildRepairTest ($prompt.Contains("-x $task")) "Prompt does not exclude task: $task"
    }
    Assert-BuildRepairTest ($prompt.Contains('with the same exclusions on every run')) 'Verification builds can lose the exclusions'
    Assert-BuildRepairTest ($prompt.Contains('do not run frontend or AI tests during focused verification either')) 'Focused verification can run excluded tests'
    Assert-BuildRepairTest (-not $prompt.Contains('with no test/task exclusions')) 'Prompt contradicts the task exclusions'
    Assert-BuildRepairTest (-not $prompt.Contains('rerun the SAME full build on the integrated HEAD')) 'Prompt requires full rebuilds in the push retry loop'
    $schema = New-BuildRepairSchema 25 | ConvertFrom-Json
    Assert-BuildRepairTest ($schema.properties.failures_targeted.maximum -eq 25) 'Schema ignored the batch limit'
    Assert-BuildRepairThrows { Invoke-BuildRepairLoop -Root $root -MaxFailuresPerBatch 101 } 'greater than|range'
    Assert-BuildRepairThrows { Invoke-BuildRepairLoop -Root $root -MaxFailuresPerBatch 0 } 'less than|range'

    $root = Invoke-BuildRepairTestScenario 'partial_then_green'
    Assert-BuildRepairTest ($script:calls -eq 2 -and $script:directories[0] -ne $script:directories[1]) 'Partial push did not start a fresh batch'
    $remoteHead = (Get-BuildRepairGitText $root @('ls-remote', 'origin', 'refs/heads/main')) -split '\s+'
    Assert-BuildRepairTest ($remoteHead[0] -eq (Get-BuildRepairGitText $root @('rev-parse', 'HEAD'))) 'Partial repair was not pushed to the local bare remote'

    $cases = @{
        no_progress = 'verified repair progress'; no_commit_progress = 'no commit progress'
        over_budget = 'batch limit'; false_green = 'Green contradicts'; stale_head = 'Current HEAD differs'
        different_pushed_head = 'verified commit differs'; missing_log = 'build log is missing'
        failed_green_log = 'does not confirm green'; mixed_log = 'does not confirm green'
        missing_result = 'no result.json'; invalid_json = 'Invalid|JSON'
        codex_failure = 'Codex execution failure'; dirty_after = 'must be clean'
        blocked = 'Dependency download unavailable'; push_failure = 'Push rejected after twenty attempts'
        unconfirmed_push = 'does not confirm the reported push'
    }
    foreach ($case in $cases.GetEnumerator()) { Invoke-BuildRepairTestScenario $case.Key $case.Value | Out-Null }
    $blockedRoot = Join-Path $testRoot 'blocked'
    Assert-BuildRepairTest ((Get-Content -LiteralPath (Join-Path $blockedRoot 'repair.txt') -Raw) -eq 'preserved diagnostic work') 'Blocked work was discarded'
    $pushFailedRoot = Join-Path $testRoot 'push_failure'
    Assert-BuildRepairTest ((Get-BuildRepairGitText $pushFailedRoot @('log', '-1', '--format=%s')) -eq 'Preserved unpushed repair') 'Unpushed commit was discarded'

    $script:raceTrace = @()
    $root = Invoke-BuildRepairTestScenario 'push_race'
    Assert-BuildRepairTest (($script:raceTrace -join ',') -eq 'build-before-rebase,resolve-conflict,focused-check-after-rebase,push') 'Push retry did not use focused validation after conflict resolution'
    $raceResult = Get-Content -LiteralPath (Join-Path $script:directories[0] 'result.json') -Raw | ConvertFrom-Json
    Assert-BuildRepairTest ($raceResult.full_build_head -ne $raceResult.pushed_head) 'Result hides the original full-build commit'
    Assert-BuildRepairTest ((Get-Content -LiteralPath (Join-Path $script:directories[0] 'build.log') -Raw) -ceq "BUILD SUCCESSFUL in 1s`n") 'Integration overwrote the original full-build log'
    Assert-BuildRepairTest ((Get-Content -LiteralPath (Join-Path $root 'repair.txt') -Raw) -match 'concurrent behavior\s+local verified repair') 'Conflict resolution lost one side'
    Invoke-BuildRepairTestScenario 'stale_after_rebase' 'verified commit differs' | Out-Null
    Invoke-BuildRepairTestScenario 'missing_integration_log' 'requires an integration validation log' | Out-Null
    Invoke-BuildRepairTestScenario 'stale_integration_log' 'does not confirm the current HEAD' | Out-Null

    $root = New-BuildRepairTestCheckout 'preflight'
    $script:calls = 0
    Write-BuildRepairText (Join-Path $root 'untracked.txt') 'manual work'
    Assert-BuildRepairThrows { Invoke-BuildRepairLoop -Root $root } 'must be clean'
    Remove-Item -LiteralPath (Join-Path $root 'untracked.txt')
    Get-BuildRepairGitText $root @('checkout', '-b', 'test-branch') | Out-Null
    Assert-BuildRepairThrows { Invoke-BuildRepairLoop -Root $root } 'must run on main'
    Get-BuildRepairGitText $root @('checkout', 'main') | Out-Null
    foreach ($marker in @('rebase-merge', 'rebase-apply', 'MERGE_HEAD')) {
        $path = Get-BuildRepairGitText $root @('rev-parse', '--path-format=absolute', '--git-path', $marker)
        Write-BuildRepairText $path 'unfinished operation'
        Assert-BuildRepairThrows { Invoke-BuildRepairLoop -Root $root } 'unfinished Git operation'
        Remove-Item -LiteralPath $path
    }
    Get-BuildRepairGitText $root @('remote', 'remove', 'origin') | Out-Null
    Assert-BuildRepairThrows { Invoke-BuildRepairLoop -Root $root } 'Git remote get-url origin failed'
    Assert-BuildRepairTest ($script:calls -eq 0) 'A preflight failure launched Codex'
    Write-Host 'PASS dirty checkout, wrong branch, unfinished operations, missing remote'

    $head = Get-BuildRepairGitText $root @('rev-parse', 'HEAD')
    $resultPath = Join-Path $testRoot 'validation.json'
    $mutations = @(
        { param($r) $r.has_remaining_failures = 'false' },
        { param($r) $r.failures_targeted = '0' },
        { param($r) $r.failures_fixed = -1 },
        { param($r) $r.failures_targeted = 0; $r.failures_fixed = 1 },
        { param($r) $r.build_exit_code = $null },
        { param($r) $r.build_exit_code = 1 },
        { param($r) $r.summary = '' },
        { param($r) $r.verified_head = '1234' },
        { param($r) $r.full_build_head = '1234' },
        { param($r) $r.full_build_head = $null },
        { param($r) $r.pushed_head = $null },
        { param($r) $r.blocking_reason = 'contradiction' },
        { param($r) $r.status = 'blocked'; $r.blocking_reason = $null },
        { param($r) $r.status = 'remaining_failures'; $r.has_remaining_failures = $true; $r.failures_targeted = 1; $r.failures_fixed = 1 },
        { param($r) $r.PSObject.Properties.Remove('build_exit_code') },
        { param($r) $r | Add-Member -NotePropertyName extra -NotePropertyValue $true }
    )
    foreach ($mutation in $mutations) {
        $result = New-BuildRepairTestResult $head
        & $mutation $result
        Write-BuildRepairText $resultPath ($result | ConvertTo-Json)
        Assert-BuildRepairThrows { Read-BuildRepairResult $resultPath 100 } '.'
    }
    Write-BuildRepairText $resultPath '[]'
    Assert-BuildRepairThrows { Read-BuildRepairResult $resultPath 100 } '.'
    Write-Host 'PASS strict result types, fields, hashes, verdicts, and batch counts'

    $nativeDirectory = Join-Path $testRoot 'fake codex'
    New-Item -ItemType Directory -Path $nativeDirectory | Out-Null
    $fakeCodexPath = Join-Path $nativeDirectory 'fake-codex.ps1'
    Write-BuildRepairText $fakeCodexPath @'
param([string] $Outcome)
[Console]::InputEncoding = [System.Text.UTF8Encoding]::new($false)
$inputText = [Console]::In.ReadToEnd()
[System.IO.File]::WriteAllText((Join-Path $PSScriptRoot 'received.txt'), $inputText, [System.Text.UTF8Encoding]::new($false))
[Console]::WriteLine('non-JSON diagnostic')
switch ($Outcome) {
    'completed' { [Console]::WriteLine('{"type":"turn.completed"}'); exit 0 }
    'failed' { [Console]::WriteLine('{"type":"turn.failed"}'); exit 1 }
    'nonzero' { [Console]::WriteLine('{"type":"turn.completed"}'); exit 7 }
    'missing_terminal' { exit 0 }
}
'@
    $nativePrompt = 'Prompt with unicode: ' + [char]0x151 + [char]0x1F0 + ' and a path with spaces.'
    Write-BuildRepairText (Join-Path $nativeDirectory 'prompt.txt') $nativePrompt
    foreach ($outcome in @('completed', 'failed', 'nonzero', 'missing_terminal')) {
        $startInfo = New-Object System.Diagnostics.ProcessStartInfo
        $startInfo.FileName = (Get-Command powershell.exe).Source
        $startInfo.Arguments = (@('-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', $fakeCodexPath, '-Outcome', $outcome) | ForEach-Object { ConvertTo-BuildRepairArgument $_ }) -join ' '
        $startInfo.UseShellExecute = $false
        $startInfo.CreateNoWindow = $true
        $startInfo.RedirectStandardInput = $true
        $startInfo.RedirectStandardOutput = $true
        $startInfo.StandardOutputEncoding = [System.Text.UTF8Encoding]::new($false)
        if ($outcome -eq 'completed') { Invoke-BuildRepairCodexProcess $startInfo $nativeDirectory }
        else { Assert-BuildRepairThrows { Invoke-BuildRepairCodexProcess $startInfo $nativeDirectory } 'did not complete successfully' }
        Assert-BuildRepairTest ([System.IO.File]::ReadAllText((Join-Path $nativeDirectory 'received.txt')) -ceq $nativePrompt) 'Native stdin lost Unicode or spaces'
        Assert-BuildRepairTest ((Get-Content -LiteralPath (Join-Path $nativeDirectory 'events.jsonl') -Raw) -match 'non-JSON diagnostic') 'Native output was not preserved'
    }
    Write-Host 'PASS native UTF-8 stdin, spaced paths, terminal events, and nonzero exit handling'
    Write-Host 'All build repair tests passed. No live Codex, project builds, or external pushes were used.'
}
finally {
    $resolvedTestRoot = [System.IO.Path]::GetFullPath($testRoot)
    if (-not $resolvedTestRoot.StartsWith($testParent + [System.IO.Path]::DirectorySeparatorChar, [System.StringComparison]::OrdinalIgnoreCase)) { throw 'Refusing to remove a test directory outside the build repair test workspace' }
    Remove-Item -LiteralPath $resolvedTestRoot -Recurse -Force
}
