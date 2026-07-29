package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Okk;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("scryfall")
class RandomAiDecisionEngineTest {

    @Test
    void doesNotDeclareOkkWithoutGreaterPowerBlocker() {
        GameTestHarness harness = new GameTestHarness();
        GameData gameData = harness.getGameData();
        Player attackerPlayer = harness.getPlayer1();
        Player aiPlayer = harness.getPlayer2();

        Permanent attacker = harness.addToBattlefieldAndReturn(attackerPlayer, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent okk = harness.addToBattlefieldAndReturn(aiPlayer, new Okk());
        okk.setSummoningSick(false);

        harness.forceActivePlayer(attackerPlayer);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        RandomAiDecisionEngine engine = new RandomAiDecisionEngine(
                gameData.id,
                aiPlayer,
                harness.getGameRegistry(),
                harness.getGameService(),
                harness.getGameQueryService(),
                harness.getBlockLegalityService(),
                harness.getCombatAttackService(),
                harness.getGameActionAvailabilityService(),
                harness.getCastingCostService(),
                harness.getCastingPermissionService(),
                harness.getTargetValidationService(),
                harness.getTargetLegalityService(),
                new Random() {
                    @Override
                    public boolean nextBoolean() {
                        return true;
                    }
                },
                new FuzzTelemetry());

        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.BLOCKER_DECLARATION);

            assertThat(watcher.drainFailures()).isEmpty();
            assertThat(gameData.interaction.isAwaitingInput()).isFalse();
            assertThat(okk.isBlocking()).isFalse();
        } finally {
            watcher.uninstall();
        }
    }
}
