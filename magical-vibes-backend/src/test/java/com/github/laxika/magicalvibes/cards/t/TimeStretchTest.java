package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ExtraTurnEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

class TimeStretchTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        harness.skipMulligan();
        harness.clearMessages();
    }

    /**
     * Sets auto-stop on PRECOMBAT_MAIN for both players so auto-pass
     * stops predictably once per turn, giving tests manual control
     * over turn advancement.
     */
    private void enableAutoStop() {
        GameData gd = harness.getGameData();
        Set<TurnStep> stops1 = ConcurrentHashMap.newKeySet();
        stops1.add(TurnStep.PRECOMBAT_MAIN);
        gd.playerAutoStopSteps.put(player1.getId(), stops1);
        Set<TurnStep> stops2 = ConcurrentHashMap.newKeySet();
        stops2.add(TurnStep.PRECOMBAT_MAIN);
        gd.playerAutoStopSteps.put(player2.getId(), stops2);
    }

    /**
     * Ends the current turn by forcing to cleanup and passing both priorities.
     * With auto-stop enabled, this advances exactly one turn and stops
     * at the next player's PRECOMBAT_MAIN.
     */
    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Time Stretch has correct card properties")
    void hasCorrectProperties() {
        TimeStretch card = new TimeStretch();

        assertThat(card.getName()).isEqualTo("Time Stretch");
        assertThat(card.getType()).isEqualTo(CardType.SORCERY);
        assertThat(card.getManaCost()).isEqualTo("{8}{U}{U}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLUE);
        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(ExtraTurnEffect.class);
        ExtraTurnEffect effect = (ExtraTurnEffect) card.getEffects(EffectSlot.SPELL).get(0);
        assertThat(effect.count()).isEqualTo(2);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts Time Stretch on the stack targeting a player")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new TimeStretch()));
        harness.addMana(player1, ManaColor.BLUE, 10);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, player1.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Time Stretch");
        assertThat(entry.getTargetPermanentId()).isEqualTo(player1.getId());
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Time Stretch goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        harness.setHand(player1, List.of(new TimeStretch()));
        harness.addMana(player1, ManaColor.BLUE, 10);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Time Stretch"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Game log records extra turns granted")
    void gameLogRecordsExtraTurns() {
        harness.setHand(player1, List.of(new TimeStretch()));
        harness.addMana(player1, ManaColor.BLUE, 10);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log ->
                log.contains("extra turn") && log.contains("2"));
    }

    // ===== Extra turn progression — targeting self =====

    @Test
    @DisplayName("Resolving targeting self queues two extra turns")
    void resolvingTargetingSelfQueuesTwoExtraTurns() {
        enableAutoStop();
        harness.setHand(player1, List.of(new TimeStretch()));
        harness.addMana(player1, ManaColor.BLUE, 10);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        // Still on caster's turn (auto-stop at PRECOMBAT_MAIN), extra turns queued
        GameData gd = harness.getGameData();
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.extraTurns).hasSize(2);
        assertThat(gd.extraTurns).containsExactly(player1.getId(), player1.getId());
    }

    @Test
    @DisplayName("First extra turn is taken by the caster after current turn ends")
    void firstExtraTurnTakenByCaster() {
        enableAutoStop();
        harness.setHand(player1, List.of(new TimeStretch()));
        harness.addMana(player1, ManaColor.BLUE, 10);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameData gd = harness.getGameData();
        int turnBefore = gd.turnNumber;

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        // End the current turn to start the first extra turn
        advanceTurn();

        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.turnNumber).isEqualTo(turnBefore + 1);
        assertThat(gd.extraTurns).hasSize(1);
    }

    @Test
    @DisplayName("Second extra turn is also taken by the caster")
    void secondExtraTurnAlsoTakenByCaster() {
        enableAutoStop();
        harness.setHand(player1, List.of(new TimeStretch()));
        harness.addMana(player1, ManaColor.BLUE, 10);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameData gd = harness.getGameData();
        int turnBefore = gd.turnNumber;

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        // End current turn → first extra turn
        advanceTurn();
        // End first extra turn → second extra turn
        advanceTurn();

        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.turnNumber).isEqualTo(turnBefore + 2);
        assertThat(gd.extraTurns).isEmpty();
    }

    @Test
    @DisplayName("Normal turn order resumes after both extra turns are consumed")
    void normalTurnOrderResumesAfterExtraTurns() {
        enableAutoStop();
        harness.setHand(player1, List.of(new TimeStretch()));
        harness.addMana(player1, ManaColor.BLUE, 10);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameData gd = harness.getGameData();
        int turnBefore = gd.turnNumber;

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        // End current turn → first extra turn
        advanceTurn();
        // End first extra turn → second extra turn
        advanceTurn();
        // End second extra turn → normal turn (opponent)
        advanceTurn();

        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.turnNumber).isEqualTo(turnBefore + 3);
        assertThat(gd.extraTurns).isEmpty();
    }

    // ===== Extra turn progression — targeting opponent =====

    @Test
    @DisplayName("Extra turns targeting opponent give them back-to-back turns")
    void extraTurnsTargetingOpponent() {
        enableAutoStop();
        harness.setHand(player1, List.of(new TimeStretch()));
        harness.addMana(player1, ManaColor.BLUE, 10);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameData gd = harness.getGameData();
        int turnBefore = gd.turnNumber;

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // End current turn → first extra turn for player2
        advanceTurn();
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.turnNumber).isEqualTo(turnBefore + 1);

        // End first extra turn → second extra turn for player2
        advanceTurn();
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.turnNumber).isEqualTo(turnBefore + 2);

        // End second extra turn → normal alternation (player1's turn)
        advanceTurn();
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.turnNumber).isEqualTo(turnBefore + 3);
    }

    // ===== Stacking with multiple extra turn effects =====

    @Test
    @DisplayName("Two Time Stretches targeting the same player give four extra turns")
    void twoTimeStretchesStackCorrectly() {
        enableAutoStop();
        harness.setHand(player1, List.of(new TimeStretch(), new TimeStretch()));
        harness.addMana(player1, ManaColor.BLUE, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameData gd = harness.getGameData();
        int turnBefore = gd.turnNumber;

        // Cast first, resolve → queue: [P1, P1]
        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        // End current turn → first extra turn (from 1st Time Stretch)
        advanceTurn();
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());

        // Cast second Time Stretch during extra turn (mana drained on turn change)
        harness.addMana(player1, ManaColor.BLUE, 10);
        // Cast → queue: [P1(2nd), P1(2nd), P1(1st)]
        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        // End first extra turn → second Time Stretch's first extra turn
        advanceTurn();
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());

        // Second Time Stretch's second extra turn
        advanceTurn();
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());

        // First Time Stretch's remaining extra turn
        advanceTurn();
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.turnNumber).isEqualTo(turnBefore + 4);

        // Normal turn order resumes
        advanceTurn();
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Extra turns from different targets interleave correctly (LIFO)")
    void extraTurnsFromDifferentTargetsInterleaveLIFO() {
        enableAutoStop();
        harness.setHand(player1, List.of(new TimeStretch(), new TimeStretch()));
        harness.addMana(player1, ManaColor.BLUE, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Cast first Time Stretch targeting self → queue: [P1, P1]
        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        // End current turn → first extra turn (P1)
        advanceTurn();

        // Cast second Time Stretch targeting opponent (mana drained on turn change)
        harness.addMana(player1, ManaColor.BLUE, 10);
        // → queue: [P2, P2, P1]
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // End first extra turn → P2's turns come first (LIFO)
        advanceTurn();
        assertThat(harness.getGameData().activePlayerId).isEqualTo(player2.getId());

        advanceTurn();
        assertThat(harness.getGameData().activePlayerId).isEqualTo(player2.getId());

        // Then P1's remaining extra turn
        advanceTurn();
        assertThat(harness.getGameData().activePlayerId).isEqualTo(player1.getId());

        // Normal turn order resumes
        advanceTurn();
        assertThat(harness.getGameData().activePlayerId).isEqualTo(player2.getId());
    }
}

