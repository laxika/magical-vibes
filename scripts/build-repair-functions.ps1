function ConvertTo-BuildRepairArgument {
    param([AllowEmptyString()][string] $Value)
    if ($Value.Length -gt 0 -and $Value -notmatch '[\s"]') { return $Value }
    $escaped = $Value -replace '(\\*)"', '$1$1\"'
    $escaped = $escaped -replace '(\\+)$', '$1$1'
    return '"' + $escaped + '"'
}

function Write-BuildRepairText {
    param([string] $Path, [string] $Text)
    [System.IO.File]::WriteAllText($Path, $Text, [System.Text.UTF8Encoding]::new($false))
}

function Get-BuildRepairGitText {
    param([string] $Root, [string[]] $Arguments)
    $startInfo = New-Object System.Diagnostics.ProcessStartInfo
    $startInfo.FileName = (Get-Command git -ErrorAction Stop).Source
    $startInfo.Arguments = (@('-C', $Root) + $Arguments | ForEach-Object { ConvertTo-BuildRepairArgument $_ }) -join ' '
    $startInfo.UseShellExecute = $false
    $startInfo.RedirectStandardOutput = $true
    $startInfo.RedirectStandardError = $true
    $startInfo.StandardOutputEncoding = [System.Text.UTF8Encoding]::new($false)
    $startInfo.StandardErrorEncoding = [System.Text.UTF8Encoding]::new($false)
    $startInfo.CreateNoWindow = $true
    $process = New-Object System.Diagnostics.Process
    $process.StartInfo = $startInfo
    try {
        if (-not $process.Start()) { throw 'Could not start Git.' }
        $stdoutTask = $process.StandardOutput.ReadToEndAsync()
        $stderrTask = $process.StandardError.ReadToEndAsync()
        $process.WaitForExit()
        $stdout = $stdoutTask.GetAwaiter().GetResult()
        $stderr = $stderrTask.GetAwaiter().GetResult()
        if ($process.ExitCode -ne 0) { throw "Git $($Arguments -join ' ') failed: $($stderr.Trim())" }
        return $stdout.Trim()
    }
    finally { $process.Dispose() }
}

function Assert-BuildRepairCheckout {
    param([string] $Root)
    if (-not (Test-Path -LiteralPath (Join-Path $Root 'gradlew.bat') -PathType Leaf)) { throw 'The Gradle wrapper gradlew.bat was not found.' }
    if ((Get-BuildRepairGitText $Root @('branch', '--show-current')) -ne 'main') { throw 'Build repairs must run on main.' }
    Get-BuildRepairGitText $Root @('remote', 'get-url', 'origin') | Out-Null
    foreach ($marker in @('MERGE_HEAD', 'CHERRY_PICK_HEAD', 'REVERT_HEAD', 'rebase-merge', 'rebase-apply', 'sequencer')) {
        $path = Get-BuildRepairGitText $Root @('rev-parse', '--path-format=absolute', '--git-path', $marker)
        if (Test-Path -LiteralPath $path) { throw "An unfinished Git operation exists ($marker); work was preserved." }
    }
    if (Get-BuildRepairGitText $Root @('status', '--porcelain', '--untracked-files=all')) { throw 'The working tree must be clean; work was preserved.' }
}

function New-BuildRepairSchema {
    param([ValidateRange(1, 100)][int] $MaxFailuresPerBatch)
    return (@{
        type = 'object'
        additionalProperties = $false
        properties = @{
            status = @{ type = 'string'; enum = @('green', 'remaining_failures', 'blocked') }
            failures_targeted = @{ type = 'integer'; minimum = 0; maximum = $MaxFailuresPerBatch }
            failures_fixed = @{ type = 'integer'; minimum = 0; maximum = $MaxFailuresPerBatch }
            has_remaining_failures = @{ type = 'boolean' }
            build_exit_code = @{ type = @('integer', 'null') }
            full_build_head = @{ type = @('string', 'null') }
            verified_head = @{ type = @('string', 'null') }
            pushed_head = @{ type = @('string', 'null') }
            summary = @{ type = 'string' }
            blocking_reason = @{ type = @('string', 'null') }
        }
        required = @('status', 'failures_targeted', 'failures_fixed', 'has_remaining_failures', 'build_exit_code', 'full_build_head', 'verified_head', 'pushed_head', 'summary', 'blocking_reason')
    } | ConvertTo-Json -Depth 6)
}

function New-BuildRepairPrompt {
    param([string] $Directory, [int] $MaxFailuresPerBatch)
    return @"
Perform exactly one automated pull/build/repair/commit/push batch in this repository on main.
The user explicitly authorizes backend builds and tests with frontend tasks and AI tests excluded, committing repair batches, pushing origin/main, and resolving merge/rebase conflicts for this task. This overrides AGENTS.md's defaults about asking the user to run the full suite and waiting for commit permission within this scope. Follow all other repository instructions. Do not ask clarifying questions: perform the work and return the required JSON. Do not launch other agents. Every reference to a full build below means the scoped build command in step 2, with the same exclusions on every run. Green reports success only within this scope.
origin/main is especially busy: expect multiple pushes per minute. Full rebuilds between rebases cannot keep up. Complete full-build verification before entering the push retry loop, then use rapid pull/rebase/push retries with review and only necessary focused checks.

All batch artifacts belong in this directory, which is ignored by Git:
$Directory
Save complete combined stdout/stderr from every full build in numbered build logs here. Also overwrite build.log with the complete output of the LAST full build; never append multiple builds to it. Record the native Gradle exit code immediately, independently of any output pipeline. Keep result.json, schema.json, prompt.txt, and events.jsonl intact; the outer script owns them.

1. Confirm a clean checkout on main with no unfinished Git operations. Run git pull --rebase origin main. Resolve conflicts by understanding both sides and preserving intended behavior, then continue the rebase. Do not change branches, force-push, skip conflicting commits, discard unrelated changes, or change Git remotes/configuration. If correct conflict resolution is blocked, return blocked and preserve work with an explanation.
2. Run .\gradlew.bat build --continue --rerun-tasks --console=plain -x :magical-vibes-frontend:buildAngular -x :magical-vibes-frontend:testAngular -x :magical-vibes-application:copyFrontend -x :magical-vibes-ai:test from the repository root. This keeps card, engine, and other backend tests enabled while skipping frontend build/test tasks and AI tests. Do not add other test/task exclusions, test filters, dry-run, injected init scripts, opt-in fuzz/stress/benchmark properties, Angular tasks, or cleanNode. Inspect this run's logs and fresh test reports, not stale reports for excluded tasks. Frontend and AI test reports are outside this batch's scope. Environment/dependency/network/resource failures that cannot be repaired correctly are blocked, not green.
3. If the build fails, select at most $MaxFailuresPerBatch distinct failures to repair during THIS batch. Count each failing test case individually and non-test failures by distinct diagnostic/root cause. Keep a list of selected failures in batch-notes.txt. Never expand this repair selection beyond the limit even if a later build exposes new failures. A shared fix may incidentally resolve other failures; describe these in the summary, but failures_fixed counts only verified fixes from the selected list and must not exceed failures_targeted. Once the batch limit is reached, finish verification and push; do not keep repairing until everything is green within this session.
4. Diagnose and implement rules-correct repairs, add focused behavioral regression tests where appropriate, and run focused checks to verify the selected repairs. Focus on card and engine behavior; do not run frontend or AI tests during focused verification either. Never disable checks, remove coverage, skip failing tests within the selected scope, or weaken assertions just to obtain a pass. Read agent-docs/ARCHITECTURE.md before engine/service/domain changes; verify ambiguous Magic behavior using official rulings and verify any CR numbers through the rules MCP. Avoid unrelated refactors and frontend/AI changes. If no selected failure can be fixed, return blocked with the reason rather than claiming progress.
5. Review the diff and commit the actual verified repair batch, even if other failures remain. Commit messages must use short set codes, never full card set names, and include Co-authored-by: OpenAI Codex <codex@openai.com>. Write multiline commit messages to a file here and use git commit --file. Do not create an empty commit. Only stage intended repairs; ignored artifacts must stay untracked. If the initial build was already green, no repair commit is needed.
6. After the final repair commit, run the SAME full build again on the exact current HEAD before entering the push retry loop. An already-green initial build needs no duplicate run if HEAD and the working tree did not change. The final full build may still fail on leftover failures; ensure the selected repairs are verified, then push the partial batch. If a build changes tracked files, review/commit only necessary changes and rebuild the new HEAD before entering the retry loop. full_build_head is the full hash of the commit used by the last full build; build_exit_code and build.log always describe that build. Initially set verified_head=full_build_head. Leave a clean checkout.
7. Run git push origin main even for an already-green initial build (there may be existing local commits). On a non-fast-forward rejection, immediately pull --rebase origin main, resolve conflicts correctly, review the incoming changes and the rebased repair diff, and retry the push promptly. Do not run a full build between rebases or push retries. Reuse previous validation when incoming changes are independent of the repairs. If conflicts or incoming changes affect repaired behavior, run only the necessary focused checks before retrying; preserve all earlier frontend/AI exclusions. If focused checks cannot establish correctness, return blocked and preserve work. Record each integration review and any focused commands/results in rebase-validation.log, ending the log with a line 'Verified HEAD: <full hash>' for the latest reviewed and validated HEAD. Set verified_head to that HEAD; keep full_build_head unchanged. Use at most TWENTY push attempts total in this batch. Do not repair newly discovered failures outside the selection. Other push errors, unresolved conflicts, or exhausted retries mean blocked. Never force-push. pushed_head is the full HEAD hash of the successful push; use null if no push succeeded. Leave a clean checkout. If integration prevents a selected repair from being verified, return blocked instead of pushing an unverified repair.

Return only the schema-constrained JSON result. status=green requires a successful last full build (exit code 0), verified_head=pushed_head=current HEAD, a successful push, a clean checkout, and no unfinished Git operation. has_remaining_failures must be false for green. If full_build_head differs from verified_head, rebase-validation.log must document validation carried forward through integration. Green does not imply that the rebased pushed HEAD received a full rebuild; state this limitation in summary.
status=remaining_failures requires a nonzero last full-build exit code, has_remaining_failures=true, at least one verified selected fix, a clean checkout, and a successful push of verified_head, with the same integration evidence when full_build_head differs. The outer script starts a fresh session to pull/build/repair the next batch; you must not start another batch yourself.
status=blocked requires a nonempty blocking_reason and must never report green or invite an automatic retry. Use null for build_exit_code/full_build_head/verified_head if no full build completed. For completed non-blocked batches blocking_reason must be null. summary must briefly explain actual repairs, verification, and leftovers, distinguishing the full-build commit from any later integration validation. Preserve logs and unfinished changes on any failure.
"@
}

function Invoke-BuildRepairCodex {
    param([string] $Root, [string] $Directory, [string] $Model, [string] $Effort)
    $arguments = @('--search', 'exec', '--approve-for-me', '--ephemeral', '--json', '--model', $Model,
        '--config', "model_reasoning_effort=`"$Effort`"", '--cd', $Root,
        '--output-schema', (Join-Path $Directory 'schema.json'),
        '--output-last-message', (Join-Path $Directory 'result.json'), '-')
    $startInfo = New-Object System.Diagnostics.ProcessStartInfo
    $startInfo.FileName = (Get-Command codex -ErrorAction Stop).Source
    $startInfo.Arguments = ($arguments | ForEach-Object { ConvertTo-BuildRepairArgument $_ }) -join ' '
    $startInfo.WorkingDirectory = $Root
    $startInfo.UseShellExecute = $false
    $startInfo.RedirectStandardInput = $true
    $startInfo.RedirectStandardOutput = $true
    $startInfo.StandardOutputEncoding = [System.Text.UTF8Encoding]::new($false)
    $startInfo.CreateNoWindow = $true
    Invoke-BuildRepairCodexProcess -StartInfo $startInfo -Directory $Directory
}

function Invoke-BuildRepairCodexProcess {
    param([System.Diagnostics.ProcessStartInfo] $StartInfo, [string] $Directory)
    $process = New-Object System.Diagnostics.Process
    $process.StartInfo = $StartInfo
    $writer = [System.IO.StreamWriter]::new((Join-Path $Directory 'events.jsonl'), $false, [System.Text.UTF8Encoding]::new($false))
    $started = $false
    $completed = $false
    $terminal = $false
    try {
        $started = $process.Start()
        if (-not $started) { throw 'Could not start Codex.' }
        # .NET Framework's ProcessStartInfo has no StandardInputEncoding property.
        $inputWriter = [System.IO.StreamWriter]::new($process.StandardInput.BaseStream, [System.Text.UTF8Encoding]::new($false))
        try { $inputWriter.Write([System.IO.File]::ReadAllText((Join-Path $Directory 'prompt.txt'))) }
        finally { $inputWriter.Dispose() }
        while ($true) {
            # Read asynchronously so a quiet long-running build still gets a heartbeat.
            $readTask = $process.StandardOutput.ReadLineAsync()
            while (-not $readTask.Wait(60000)) { Write-Host "Codex is still working. Logs: $Directory" }
            $line = $readTask.GetAwaiter().GetResult()
            if ($null -eq $line) { break }
            $writer.WriteLine($line)
            $writer.Flush()
            try { $event = $line | ConvertFrom-Json } catch { continue }
            switch ($event.type) {
                'thread.started' { Write-Host "Codex thread: $($event.thread_id)" }
                'item.completed' {
                    if ($event.item.type -eq 'agent_message') { Write-Host $event.item.text }
                    elseif ($event.item.type -eq 'command_execution') { Write-Host "Codex command $($event.item.status): $($event.item.command)" }
                }
                'error' { Write-Warning "Codex error: $($event.message)" }
                'turn.failed' { $terminal = $true }
                'turn.completed' { $terminal = $true; $completed = $true }
            }
            if ($terminal) { break }
        }
        # A descendant can retain the output pipe after the terminal JSON event.
        if (-not $process.WaitForExit(10000)) {
            $process.Kill()
            $process.WaitForExit()
            throw 'Codex did not exit after its output ended; logs and work were preserved.'
        }
        if (-not $completed -or $process.ExitCode -ne 0) { throw "Codex did not complete successfully (exit $($process.ExitCode))." }
    }
    finally {
        if ($started -and -not $process.HasExited) { $process.Kill(); $process.WaitForExit() }
        $writer.Dispose()
        $process.Dispose()
    }
}

function Read-BuildRepairResult {
    param([string] $Path, [int] $MaxFailuresPerBatch)
    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) { throw 'Codex produced no result.json.' }
    $result = Get-Content -LiteralPath $Path -Raw -Encoding UTF8 | ConvertFrom-Json
    $required = @('status', 'failures_targeted', 'failures_fixed', 'has_remaining_failures', 'build_exit_code', 'full_build_head', 'verified_head', 'pushed_head', 'summary', 'blocking_reason')
    if ($result -isnot [pscustomobject] -or @(Compare-Object $required @($result.PSObject.Properties.Name)).Count -ne 0) { throw 'Invalid result fields; a JSON object with exactly the required fields is expected.' }
    if ($result.status -isnot [string] -or $result.status -notin @('green', 'remaining_failures', 'blocked')) { throw 'Invalid repair status.' }
    foreach ($field in @('failures_targeted', 'failures_fixed', 'build_exit_code')) {
        $value = $result.$field
        if ($field -eq 'build_exit_code' -and $null -eq $value -and $result.status -eq 'blocked') { continue }
        if ($value -isnot [int] -and $value -isnot [long]) { throw "$field must be an integer." }
        if ($field -ne 'build_exit_code' -and ($value -lt 0 -or $value -gt $MaxFailuresPerBatch)) { throw "$field exceeds the repair batch limit or is negative." }
    }
    if ($result.failures_fixed -gt $result.failures_targeted) { throw 'Fixed failure count exceeds the selected failures.' }
    if ($result.has_remaining_failures -isnot [bool]) { throw 'has_remaining_failures must be a boolean.' }
    if ($result.summary -isnot [string] -or [string]::IsNullOrWhiteSpace($result.summary)) { throw 'A nonempty repair summary is required.' }
    foreach ($field in @('full_build_head', 'verified_head', 'pushed_head')) {
        $value = $result.$field
        if ($null -eq $value -and $result.status -eq 'blocked') { continue }
        if ($value -isnot [string] -or $value -cnotmatch '^(?:[0-9a-f]{40}|[0-9a-f]{64})$') { throw "$field must be a full Git commit hash." }
    }
    if ($result.status -eq 'blocked') {
        if ($result.blocking_reason -isnot [string] -or [string]::IsNullOrWhiteSpace($result.blocking_reason)) { throw 'A blocked result requires a blocking_reason.' }
        return $result
    }
    if ($null -ne $result.blocking_reason) { throw 'A completed batch cannot have a blocking_reason.' }
    if ($result.verified_head -cne $result.pushed_head) { throw 'The verified commit differs from the pushed commit.' }
    if ($result.status -eq 'green') {
        if ($result.build_exit_code -ne 0 -or $result.has_remaining_failures) { throw 'Green contradicts the full-build result or remaining failures.' }
    }
    elseif ($result.build_exit_code -eq 0 -or -not $result.has_remaining_failures -or $result.failures_fixed -eq 0) {
        throw 'Remaining failures require a failed full build and verified repair progress.'
    }
    return $result
}

function Assert-BuildRepairCompletedBatch {
    param([string] $Root, [string] $Directory, $Result, [string] $StartingHead)
    Assert-BuildRepairCheckout $Root
    $head = Get-BuildRepairGitText $Root @('rev-parse', 'HEAD')
    if ($head -cne $Result.verified_head) { throw 'Current HEAD differs from the reported verified/pushed commit.' }
    if ((Get-BuildRepairGitText $Root @('rev-parse', 'refs/remotes/origin/main')) -cne $head) { throw 'origin/main does not confirm the reported push.' }
    if ($Result.status -eq 'remaining_failures' -and $head -ceq $StartingHead) { throw 'The repair batch made no commit progress.' }
    if ($Result.full_build_head -cne $head) {
        Get-BuildRepairGitText $Root @('cat-file', '-e', "$($Result.full_build_head)^{commit}") | Out-Null
        $integrationLog = Join-Path $Directory 'rebase-validation.log'
        if (-not (Test-Path -LiteralPath $integrationLog -PathType Leaf)) { throw 'Rebased HEAD requires an integration validation log.' }
        $lastLine = @(Get-Content -LiteralPath $integrationLog -Encoding UTF8 | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }) | Select-Object -Last 1
        if ($lastLine -cne "Verified HEAD: $head") { throw 'Integration validation log does not confirm the current HEAD.' }
    }
    $buildLog = Join-Path $Directory 'build.log'
    if (-not (Test-Path -LiteralPath $buildLog -PathType Leaf)) { throw 'The last full-build log is missing.' }
    $success = [bool](Select-String -LiteralPath $buildLog -Pattern '^BUILD SUCCESSFUL(?:\s|$)' -Quiet)
    $failure = [bool](Select-String -LiteralPath $buildLog -Pattern '^BUILD FAILED(?:\s|$)|^FAILURE:|^> Task .* FAILED\s*$' -Quiet)
    if ($Result.status -eq 'green' -and (-not $success -or $failure)) { throw 'The last full-build log does not confirm green.' }
    if ($Result.status -eq 'remaining_failures' -and (-not $failure -or $success)) { throw 'The last full-build log contradicts remaining failures.' }
}

function Invoke-BuildRepairLoop {
    param(
        [string] $Root,
        [ValidateNotNullOrEmpty()][string] $Model = 'gpt-6.1-sol',
        [ValidateSet('low', 'medium', 'high', 'xhigh', 'max')][string] $Effort = 'high',
        [ValidateRange(1, 100)][int] $MaxFailuresPerBatch = 100
    )
    Assert-BuildRepairCheckout $Root
    $outputRoot = Join-Path $Root 'scripts/result'
    Get-BuildRepairGitText $Root @('check-ignore', '--', 'scripts/result/build-and-fix/probe') | Out-Null
    New-Item -ItemType Directory -Force -Path $outputRoot | Out-Null
    $lockPath = Join-Path $outputRoot 'build-and-fix.lock'
    try { $lock = [System.IO.File]::Open($lockPath, [System.IO.FileMode]::OpenOrCreate, [System.IO.FileAccess]::ReadWrite, [System.IO.FileShare]::None) }
    catch { throw 'Could not acquire the repair lock; another build repair loop may be running.' }
    $directory = $null
    try {
        $runDirectory = Join-Path $outputRoot ('build-and-fix/' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '-' + [guid]::NewGuid().ToString('N'))
        for ($iteration = 1; ; $iteration++) {
            Assert-BuildRepairCheckout $Root
            $startingHead = Get-BuildRepairGitText $Root @('rev-parse', 'HEAD')
            $directory = Join-Path $runDirectory ('batch-{0:D4}' -f $iteration)
            New-Item -ItemType Directory -Force -Path $directory | Out-Null
            Write-BuildRepairText (Join-Path $directory 'schema.json') (New-BuildRepairSchema $MaxFailuresPerBatch)
            Write-BuildRepairText (Join-Path $directory 'prompt.txt') (New-BuildRepairPrompt $directory $MaxFailuresPerBatch)
            Write-Host "Batch $iteration - $Model, effort $Effort, at most $MaxFailuresPerBatch selected failures. Logs: $directory"
            Invoke-BuildRepairCodex -Root $Root -Directory $directory -Model $Model -Effort $Effort
            $result = Read-BuildRepairResult (Join-Path $directory 'result.json') $MaxFailuresPerBatch
            Write-Host $result.summary
            if ($result.status -eq 'blocked') { throw "Codex is blocked: $($result.blocking_reason)" }
            Assert-BuildRepairCompletedBatch $Root $directory $result $startingHead
            if ($result.status -eq 'green') {
                Write-Host "PASS: the backend build passed with frontend tasks and AI tests excluded on commit $($result.full_build_head)."
                if ($result.full_build_head -cne $result.pushed_head) { Write-Host "Rebase review and necessary focused checks carried validation forward to pushed commit $($result.pushed_head); no full rebuild was run after integration." }
                return
            }
            Write-Host "Pushed a batch with $($result.failures_fixed) verified fixes. Failures remain; starting a fresh Codex session."
        }
    }
    catch { throw "$($_.Exception.Message) Logs and work were preserved. Logs: $directory" }
    finally { $lock.Dispose() }
}
