package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.cards.a.AwesomePresence;
import com.github.laxika.magicalvibes.cards.b.BackFromTheBrink;
import com.github.laxika.magicalvibes.cards.c.Confiscate;
import com.github.laxika.magicalvibes.cards.e.Errantry;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.Hipparion;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.b.BerserkersOfBloodRidge;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mindslaver;
import com.github.laxika.magicalvibes.cards.o.Okk;
import com.github.laxika.magicalvibes.cards.o.OrcishConscripts;
import com.github.laxika.magicalvibes.cards.p.PhyrexianTribute;
import com.github.laxika.magicalvibes.cards.s.StormCauldron;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("scryfall")
class RandomAiDecisionEngineTest {

    @Test
    void excludesCanOnlyAttackAloneCreatureAndIgnoresStaleRetry() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player opponent = harness.getPlayer1();
        Player aiPlayer = harness.getPlayer2();
        harness.setLife(opponent, 20);

        Permanent restricted = harness.addToBattlefieldAndReturn(aiPlayer, new GrizzlyBears());
        restricted.setSummoningSick(false);
        Permanent aura = harness.addToBattlefieldAndReturn(aiPlayer, new Errantry());
        aura.setAttachedTo(restricted.getId());
        Permanent unrestricted = harness.addToBattlefieldAndReturn(aiPlayer, new GrizzlyBears());
        unrestricted.setSummoningSick(false);
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        RandomAiDecisionEngine engine = createAlwaysActivateEngine(harness, aiPlayer);
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.ATTACKER_DECLARATION);
            TurnStep stepAfterDeclaration = gameData.currentStep;

            engine.handleEvent(AiDecisionKind.ATTACKER_DECLARATION);

            assertThat(watcher.drainFailures()).isEmpty();
            assertThat(gameData.currentStep).isEqualTo(stepAfterDeclaration);
        } finally {
            watcher.uninstall();
        }

        assertThat(restricted.isAttacking()).isFalse();
        assertThat(gameData.getLife(opponent.getId())).isEqualTo(18);
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.AttackerDeclaration.class)).isNull();
    }

    @Test
    void doesNotDeclareOkkWithoutGreaterPowerBlocker() {
        GameTestHarness harness = new GameTestHarness();
        GameData gameData = harness.getGameData();
        Permanent okk = blockScenario(harness, new GrizzlyBears(), new Okk());

        declareBlockersAsRandomAi(harness);

        assertThat(gameData.interaction.isAwaitingInput()).isFalse();
        assertThat(okk.isBlocking()).isFalse();
    }

    @Test
    void doesNotDeclareOrcishConscriptsWithoutTwoOtherBlockers() {
        GameTestHarness harness = new GameTestHarness();
        GameData gameData = harness.getGameData();
        Permanent conscripts = blockScenario(harness, new HillGiant(), new OrcishConscripts());

        declareBlockersAsRandomAi(harness);

        assertThat(gameData.interaction.isAwaitingInput()).isFalse();
        assertThat(conscripts.isBlocking()).isFalse();
    }

    @Test
    void doesNotBlockHighPowerAttackerWithHipparionItCannotPayFor() {
        GameTestHarness harness = new GameTestHarness();
        GameData gameData = harness.getGameData();
        Permanent hipparion = blockScenario(harness, new HillGiant(), new Hipparion());

        declareBlockersAsRandomAi(harness);

        assertThat(gameData.interaction.isAwaitingInput()).isFalse();
        assertThat(hipparion.isBlocking()).isFalse();
    }

    @Test
    void blocksHighPowerAttackerWithHipparionWhenTheBlockCostIsPaid() {
        GameTestHarness harness = new GameTestHarness();
        GameData gameData = harness.getGameData();
        Permanent hipparion = blockScenario(harness, new HillGiant(), new Hipparion());
        harness.addMana(harness.getPlayer2(), ManaColor.WHITE, 1);

        declareBlockersAsRandomAi(harness);

        assertThat(hipparion.isBlocking()).isTrue();
        assertThat(gameData.playerManaPools.get(harness.getPlayer2().getId()).getTotal()).isZero();
    }

    @Test
    void doesNotBlockAttackerTaxedByAnAuraItCannotPayFor() {
        GameTestHarness harness = new GameTestHarness();
        GameData gameData = harness.getGameData();
        Permanent blocker = blockScenario(harness, new HillGiant(), new GrizzlyBears());
        enchantAttackerWithAwesomePresence(harness);

        declareBlockersAsRandomAi(harness);

        assertThat(gameData.interaction.isAwaitingInput()).isFalse();
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    void blocksAttackerTaxedByAnAuraWhenTheBlockCostIsPaid() {
        GameTestHarness harness = new GameTestHarness();
        GameData gameData = harness.getGameData();
        Permanent blocker = blockScenario(harness, new HillGiant(), new GrizzlyBears());
        enchantAttackerWithAwesomePresence(harness);
        harness.addMana(harness.getPlayer2(), ManaColor.GREEN, 3);

        declareBlockersAsRandomAi(harness);

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(gameData.playerManaPools.get(harness.getPlayer2().getId()).getTotal()).isZero();
    }

    @Test
    void blocksWithHipparionForFreeBelowItsPowerThreshold() {
        GameTestHarness harness = new GameTestHarness();
        Permanent hipparion = blockScenario(harness, new GrizzlyBears(), new Hipparion());

        declareBlockersAsRandomAi(harness);

        assertThat(hipparion.isBlocking()).isTrue();
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

    @Test
    void declaresAttackersForMindslaveredPlayer() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player controlled = harness.getPlayer1();
        Player aiPlayer = harness.getPlayer2();

        Permanent berserkers = mindslavedAttackerDeclaration(harness);

        RandomAiDecisionEngine engine = createAlwaysActivateEngine(harness, aiPlayer);
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.ATTACKER_DECLARATION);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(berserkers.isAttacking()).isTrue();
        assertThat(gameData.playerBattlefields.get(aiPlayer.getId())).noneMatch(Permanent::isAttacking);
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.AttackerDeclaration.class)).isNull();
        assertThat(controlled.getId()).isEqualTo(gameData.activePlayerId);
    }

    /**
     * Hands the AI seat control of player1's turn through a real Mindslaver activation and stops in
     * that turn's declare-attackers step, returning the controlled player's must-attack creature.
     *
     * <p>The engine addresses the attacker decision to the controller while the interaction stays
     * owned by the controlled player, so the AI has to declare from a battlefield that is not its
     * own. The controlled player is deliberately given more permanents than the AI seat and the
     * must-attack creature is put last, so an engine reading its own battlefield can only produce
     * indices that name something else — or nothing at all.
     */
    private Permanent mindslavedAttackerDeclaration(GameTestHarness harness) {
        GameData gameData = harness.getGameData();
        Player controlled = harness.getPlayer1();
        Player aiPlayer = harness.getPlayer2();

        Set<TurnStep> mainPhaseStops = Set.of(TurnStep.PRECOMBAT_MAIN, TurnStep.POSTCOMBAT_MAIN);
        for (Player player : List.of(controlled, aiPlayer)) {
            gameData.playerAutoStopSteps.put(player.getId(), ConcurrentHashMap.newKeySet());
            gameData.playerAutoStopSteps.get(player.getId()).addAll(mainPhaseStops);
        }

        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(aiPlayer, new Mindslaver());
        harness.addMana(aiPlayer, ManaColor.COLORLESS, 4);
        harness.activateAbility(aiPlayer, 0, null, controlled.getId());
        harness.passBothPriorities();

        // Roll into the controlled player's turn, where the control actually takes effect.
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
        assertThat(gameData.activePlayerId).isEqualTo(controlled.getId());
        assertThat(gameData.mindControlledPlayerId).isEqualTo(controlled.getId());
        assertThat(gameData.mindControllerPlayerId).isEqualTo(aiPlayer.getId());

        Permanent ownBears = harness.addToBattlefieldAndReturn(aiPlayer, new GrizzlyBears());
        ownBears.setSummoningSick(false);

        for (int i = 0; i < 3; i++) {
            harness.addToBattlefieldAndReturn(controlled, new Forest()).setSummoningSick(false);
        }
        Permanent berserkers = harness.addToBattlefieldAndReturn(controlled, new BerserkersOfBloodRidge());
        berserkers.setSummoningSick(false);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        return berserkers;
    }

    /**
     * Sets up a one-attacker combat: {@code attackerCard} attacking for {@link GameTestHarness#getPlayer1()},
     * {@code blockerCard} untapped for the AI seat. Returns the blocker so the test can assert on it.
     */
    private Permanent blockScenario(GameTestHarness harness, Card attackerCard, Card blockerCard) {
        Permanent attacker = harness.addToBattlefieldAndReturn(harness.getPlayer1(), attackerCard);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(harness.getPlayer2(), blockerCard);
        blocker.setSummoningSick(false);
        return blocker;
    }

    /**
     * Attaches an Awesome Presence controlled by the attacking player to the sole attacker, so
     * every blocker declared against it costs the AI an extra {3}.
     */
    private void enchantAttackerWithAwesomePresence(GameTestHarness harness) {
        Permanent attacker = harness.getGameData().playerBattlefields.get(harness.getPlayer1().getId()).stream()
                .filter(Permanent::isAttacking)
                .findFirst()
                .orElseThrow();
        Permanent aura = harness.addToBattlefieldAndReturn(harness.getPlayer1(), new AwesomePresence());
        aura.setAttachedTo(attacker.getId());
    }

    /**
     * Opens blocker declaration and lets the Random AI declare, asserting the engine took the
     * declaration as sent. The AI answers "yes" to every optional decision, so it declares every
     * block it believes legal; the engine rejecting one is only logged before the AI falls back to
     * no blockers, so declining to block and being refused a block are indistinguishable on the
     * board and the log is the only place they differ.
     */
    private void declareBlockersAsRandomAi(GameTestHarness harness) {
        harness.forceActivePlayer(harness.getPlayer1());
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        RandomAiDecisionEngine engine = createAlwaysActivateEngine(harness, harness.getPlayer2());
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.BLOCKER_DECLARATION);

            assertThat(watcher.drainFailures()).isEmpty();
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
