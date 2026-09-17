package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TimeStretch.class)
class TimeStretchTest extends BaseCardTest {

    /**
     * Ends the current turn and stops at the expected player's next
     * PRECOMBAT_MAIN step.
     */
    private void advanceTurn(Player expectedActivePlayer) {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(expectedActivePlayer, TurnStep.PRECOMBAT_MAIN);
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

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(player1.getId());
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Time Stretch goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        harness.setHand(player1, List.of(new TimeStretch()));
        harness.addMana(player1, ManaColor.BLUE, 10);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertInGraveyard(player1, "Time Stretch");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Game log records extra turns granted")
    void gameLogRecordsExtraTurns() {
        harness.setHand(player1, List.of(new TimeStretch()));
        harness.addMana(player1, ManaColor.BLUE, 10);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gameLogContains("2 extra turns")).isTrue();
    }

    // ===== Extra turn progression — targeting self =====

    @Test
    @DisplayName("Resolving targeting self queues two extra turns")
    void resolvingTargetingSelfQueuesTwoExtraTurns() {
        harness.setHand(player1, List.of(new TimeStretch()));
        harness.addMana(player1, ManaColor.BLUE, 10);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        // Still on the caster's turn, with the extra turns queued
        GameData gd = harness.getGameData();
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.extraTurns).hasSize(2);
        assertThat(gd.extraTurns).containsExactly(player1.getId(), player1.getId());
    }

    @Test
    @DisplayName("First extra turn is taken by the caster after current turn ends")
    void firstExtraTurnTakenByCaster() {
        harness.setHand(player1, List.of(new TimeStretch()));
        harness.addMana(player1, ManaColor.BLUE, 10);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameData gd = harness.getGameData();
        int turnBefore = gd.turnNumber;

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        // End the current turn to start the first extra turn
        advanceTurn(player1);

        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.turnNumber).isEqualTo(turnBefore + 1);
        assertThat(gd.extraTurns).hasSize(1);
    }

    @Test
    @DisplayName("Second extra turn is also taken by the caster")
    void secondExtraTurnAlsoTakenByCaster() {
        harness.setHand(player1, List.of(new TimeStretch()));
        harness.addMana(player1, ManaColor.BLUE, 10);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameData gd = harness.getGameData();
        int turnBefore = gd.turnNumber;

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        // End current turn → first extra turn
        advanceTurn(player1);
        // End first extra turn → second extra turn
        advanceTurn(player1);

        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.turnNumber).isEqualTo(turnBefore + 2);
        assertThat(gd.extraTurns).isEmpty();
    }

    @Test
    @DisplayName("Normal turn order resumes after both extra turns are consumed")
    void normalTurnOrderResumesAfterExtraTurns() {
        harness.setHand(player1, List.of(new TimeStretch()));
        harness.addMana(player1, ManaColor.BLUE, 10);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameData gd = harness.getGameData();
        int turnBefore = gd.turnNumber;

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        // End current turn → first extra turn
        advanceTurn(player1);
        // End first extra turn → second extra turn
        advanceTurn(player1);
        // End second extra turn → normal turn (opponent)
        advanceTurn(player2);

        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.turnNumber).isEqualTo(turnBefore + 3);
        assertThat(gd.extraTurns).isEmpty();
    }

    // ===== Extra turn progression — targeting opponent =====

    @Test
    @DisplayName("Extra turns targeting opponent give them back-to-back turns")
    void extraTurnsTargetingOpponent() {
        harness.setHand(player1, List.of(new TimeStretch()));
        harness.addMana(player1, ManaColor.BLUE, 10);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameData gd = harness.getGameData();
        int turnBefore = gd.turnNumber;

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // End current turn → first extra turn for player2
        advanceTurn(player2);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.turnNumber).isEqualTo(turnBefore + 1);

        // End first extra turn → second extra turn for player2
        advanceTurn(player2);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.turnNumber).isEqualTo(turnBefore + 2);

        // End second extra turn → normal alternation (player1's turn)
        advanceTurn(player1);
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.turnNumber).isEqualTo(turnBefore + 3);
    }

    // ===== Stacking with multiple extra turn effects =====

    @Test
    @DisplayName("Two Time Stretches targeting the same player give four extra turns")
    void twoTimeStretchesStackCorrectly() {
        harness.setHand(player1, List.of(new TimeStretch(), new TimeStretch()));
        harness.addMana(player1, ManaColor.BLUE, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameData gd = harness.getGameData();
        int turnBefore = gd.turnNumber;

        // Cast first, resolve → queue: [P1, P1]
        harness.castAndResolveSorcery(player1, 0, player1.getId());

        // End current turn → first extra turn (from 1st Time Stretch)
        advanceTurn(player1);
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());

        // Cast second Time Stretch during extra turn (mana drained on turn change)
        harness.addMana(player1, ManaColor.BLUE, 10);
        // Cast → queue: [P1(2nd), P1(2nd), P1(1st)]
        harness.castAndResolveSorcery(player1, 0, player1.getId());

        // End first extra turn → second Time Stretch's first extra turn
        advanceTurn(player1);
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());

        // Second Time Stretch's second extra turn
        advanceTurn(player1);
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());

        // First Time Stretch's remaining extra turn
        advanceTurn(player1);
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.turnNumber).isEqualTo(turnBefore + 4);

        // Normal turn order resumes
        advanceTurn(player2);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Extra turns from different targets interleave correctly (LIFO)")
    void extraTurnsFromDifferentTargetsInterleaveLIFO() {
        harness.setHand(player1, List.of(new TimeStretch(), new TimeStretch()));
        harness.addMana(player1, ManaColor.BLUE, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Cast first Time Stretch targeting self → queue: [P1, P1]
        harness.castAndResolveSorcery(player1, 0, player1.getId());

        // End current turn → first extra turn (P1)
        advanceTurn(player1);

        // Cast second Time Stretch targeting opponent (mana drained on turn change)
        harness.addMana(player1, ManaColor.BLUE, 10);
        // → queue: [P2, P2, P1]
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // End first extra turn → P2's turns come first (LIFO)
        advanceTurn(player2);
        assertThat(harness.getGameData().activePlayerId).isEqualTo(player2.getId());

        advanceTurn(player2);
        assertThat(harness.getGameData().activePlayerId).isEqualTo(player2.getId());

        // Then P1's remaining extra turn
        advanceTurn(player1);
        assertThat(harness.getGameData().activePlayerId).isEqualTo(player1.getId());

        // Normal turn order resumes
        advanceTurn(player2);
        assertThat(harness.getGameData().activePlayerId).isEqualTo(player2.getId());
    }
}

