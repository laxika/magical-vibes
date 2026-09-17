param([switch] $AiOnly, [switch] $TurnOnly)

$ErrorActionPreference = 'Stop'
$project = if ($AiOnly) { 'magical-vibes-ai' } else { 'magical-vibes-application' }
$tests = if ($AiOnly) {
    @('ai.AiDecisionEventSubscriberTest', 'ai.AiDecisionEngineTest', 'ai.AiGameActionsSubgameTest', 'ai.AiVsAiDecisionWiringTest', 'ai.GameSimulatorTest', 'ai.GameDataDeepCopyTest', 'ai.MCTSEngineTest')
} elseif ($TurnOnly) {
    @('cards.s.ShahrazadTest', 'cards.d.DoOverTest', 'cards.w.WizenedArbiterTest', 'cards.s.SerumPowderTest', 'service.turn.AutoPassServiceTest', 'service.GameRegistrySessionTest')
} else {
    @('cards.s.ShahrazadTest', 'cards.b.BurningWishTest', 'cards.k.KarnLiberatedTest',
      'cards.f.FrayingOmnipotenceTest', 'cards.p.PoxTest',
      'service.GameRegistrySessionTest', 'service.SubgameRequestContextTest',
      'service.ReconnectionServiceTest', 'service.GameResyncProjectionServiceTest',
      'service.GameTimeoutServiceTest', 'service.GameServiceMutationCoordinatorTest',
      'service.effect.EffectResolutionServiceTest',
      'service.input.InputCompletionServiceTest',
      'service.event.GameEventProjectionSubscriberTest', 'service.event.GameEndLifecycleSubscriberTest',
      'service.event.GameLifecycleEventSequenceTest', 'architecture.SimulationCopyCompletenessTest')
}
$buildDir = "$project/build"
New-Item -ItemType Directory -Force $buildDir | Out-Null
$log = "$buildDir/subgame-tests.log"
$selectors = ($tests | ForEach-Object { "--tests com.github.laxika.magicalvibes.$_" }) -join ' '
$started = Get-Date
& cmd /c ".\gradlew.bat :${project}:test $selectors --init-script scripts/subgame-test-sources.gradle --console=plain > `"$log`" 2>&1"
$result = $LASTEXITCODE
foreach ($test in $tests) {
    $xml = "$buildDir/test-results/test/TEST-com.github.laxika.magicalvibes.$test.xml"
    if ((Test-Path $xml) -and ($result -eq 0 -or (Get-Item $xml).LastWriteTime -gt $started)) {
        $suite = ([xml](Get-Content $xml -Raw -Encoding UTF8)).testsuite
        Write-Host "$test : $($suite.tests) tests, $($suite.failures) failures, $($suite.errors) errors"
        foreach ($case in @($suite.testcase)) {
            if ($case.failure) { Write-Host "  $($case.name): $($case.failure.message)" }
        }
    }
}
if ($result -ne 0) { Get-Content $log -Tail 45 }
Write-Host "Full log: $log"
exit $result
