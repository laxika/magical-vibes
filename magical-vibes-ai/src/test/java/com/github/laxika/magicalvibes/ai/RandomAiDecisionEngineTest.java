package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.cards.b.BackFromTheBrink;
import com.github.laxika.magicalvibes.cards.c.Confiscate;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.o.Okk;
import com.github.laxika.magicalvibes.cards.p.PhyrexianTribute;
import com.github.laxika.magicalvibes.cards.s.StormCauldron;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
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

    @Test
    void reselectsSpellTargetRemovedWhileTappingMana() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player opponent = harness.getPlayer1();
        Player aiPlayer = harness.getPlayer2();

        Permanent opponentCreature = harness.addToBattlefieldAndReturn(opponent, new GrizzlyBears());
        harness.addToBattlefield(aiPlayer, new StormCauldron());
        harness.addToBattlefield(aiPlayer, new Island());
        harness.addToBattlefield(aiPlayer, new Island());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(aiPlayer, new Forest());
        }
        harness.setHand(aiPlayer, List.of(new Confiscate()));
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

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
                    public int nextInt(int bound) {
                        return bound > 2 ? 2 : 0;
                    }
                },
                new FuzzTelemetry());

        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(watcher.drainFailures()).isEmpty();
            assertThat(gameData.stack).hasSize(1);
            assertThat(gameData.stack.getFirst().getCard().getName()).isEqualTo("Confiscate");
            assertThat(gameData.stack.getFirst().getTargetId()).isEqualTo(opponentCreature.getId());
        } finally {
            watcher.uninstall();
        }
    }

    @Test
    void passesPriorityWhenNoGraveyardCreatureManaCostIsPayable() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player aiPlayer = harness.getPlayer2();

        harness.addToBattlefield(aiPlayer, new BackFromTheBrink());
        Permanent forest = harness.addToBattlefieldAndReturn(aiPlayer, new Forest());
        forest.tap();
        harness.setGraveyard(aiPlayer, List.of(new LlanowarElves()));
        harness.setHand(aiPlayer, List.of());
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        RandomAiDecisionEngine engine = createAlwaysActivateEngine(harness, aiPlayer);

        engine.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gameData.interaction.isAwaitingInput()).isFalse();
        assertThat(gameData.priorityPassedBy).contains(aiPlayer.getId());
    }

    @Test
    void paysForAndExilesTheAffordableGraveyardCreature() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player aiPlayer = harness.getPlayer2();
        LlanowarElves affordableCreature = new LlanowarElves();

        harness.addToBattlefield(aiPlayer, new BackFromTheBrink());
        Permanent forest = harness.addToBattlefieldAndReturn(aiPlayer, new Forest());
        harness.setGraveyard(aiPlayer, List.of(affordableCreature, new GrizzlyBears()));
        harness.setHand(aiPlayer, List.of());
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        RandomAiDecisionEngine engine = createAlwaysActivateEngine(harness, aiPlayer);

        engine.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(forest.isTapped()).isTrue();
        assertThat(gameData.interaction.isAwaitingInput()).isTrue();

        engine.handleEvent(AiDecisionKind.INTERACTION);

        assertThat(gameData.interaction.isAwaitingInput()).isFalse();
        assertThat(gameData.playerGraveyards.get(aiPlayer.getId()))
                .extracting(Card::getId)
                .doesNotContain(affordableCreature.getId());
        assertThat(gameData.stack).hasSize(1);
    }

    @Test
    void castsSpellWithMultiPermanentSacrificeCost() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player opponent = harness.getPlayer1();
        Player aiPlayer = harness.getPlayer2();

        harness.addToBattlefield(opponent, new HowlingMine());
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(aiPlayer, new Swamp());
        }
        harness.addToBattlefield(aiPlayer, new GrizzlyBears());
        harness.addToBattlefield(aiPlayer, new GrizzlyBears());
        harness.setHand(aiPlayer, List.of(new PhyrexianTribute()));
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        RandomAiDecisionEngine engine = createAlwaysActivateEngine(harness, aiPlayer);

        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(watcher.drainFailures()).isEmpty();
            assertThat(gameData.stack).hasSize(1);
            assertThat(gameData.stack.getFirst().getCard().getName()).isEqualTo("Phyrexian Tribute");
            assertThat(gameData.playerGraveyards.get(aiPlayer.getId()))
                    .extracting(Card::getName)
                    .containsExactly("Grizzly Bears", "Grizzly Bears");
        } finally {
            watcher.uninstall();
        }
    }

    private RandomAiDecisionEngine createAlwaysActivateEngine(
            GameTestHarness harness, Player aiPlayer) {
        return new RandomAiDecisionEngine(
                harness.getGameData().id,
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
    }
}
