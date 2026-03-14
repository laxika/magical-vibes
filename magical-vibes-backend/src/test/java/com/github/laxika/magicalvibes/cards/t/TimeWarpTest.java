package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ExtraTurnEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

class TimeWarpTest extends BaseCardTest {

    private void enableAutoStop() {
        GameData gd = harness.getGameData();
        Set<TurnStep> stops1 = ConcurrentHashMap.newKeySet();
        stops1.add(TurnStep.PRECOMBAT_MAIN);
        gd.playerAutoStopSteps.put(player1.getId(), stops1);
        Set<TurnStep> stops2 = ConcurrentHashMap.newKeySet();
        stops2.add(TurnStep.PRECOMBAT_MAIN);
        gd.playerAutoStopSteps.put(player2.getId(), stops2);
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Time Warp has correct card properties")
    void hasCorrectProperties() {
        TimeWarp card = new TimeWarp();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(ExtraTurnEffect.class);
        ExtraTurnEffect effect = (ExtraTurnEffect) card.getEffects(EffectSlot.SPELL).get(0);
        assertThat(effect.count()).isEqualTo(1);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts Time Warp on the stack targeting a player")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new TimeWarp()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, player1.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getTargetPermanentId()).isEqualTo(player1.getId());
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Time Warp goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        harness.setHand(player1, List.of(new TimeWarp()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Time Warp"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Game log records extra turn granted")
    void gameLogRecordsExtraTurn() {
        harness.setHand(player1, List.of(new TimeWarp()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log ->
                log.contains("extra turn") && log.contains("1"));
    }

    // ===== Extra turn progression — targeting self =====

    @Test
    @DisplayName("Resolving targeting self queues one extra turn")
    void resolvingTargetingSelfQueuesOneExtraTurn() {
        enableAutoStop();
        harness.setHand(player1, List.of(new TimeWarp()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.extraTurns).hasSize(1);
        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }

    @Test
    @DisplayName("Extra turn is taken by the caster after current turn ends")
    void extraTurnTakenByCaster() {
        enableAutoStop();
        harness.setHand(player1, List.of(new TimeWarp()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameData gd = harness.getGameData();
        int turnBefore = gd.turnNumber;

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        advanceTurn();

        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.turnNumber).isEqualTo(turnBefore + 1);
        assertThat(gd.extraTurns).isEmpty();
    }

    @Test
    @DisplayName("Normal turn order resumes after extra turn is consumed")
    void normalTurnOrderResumesAfterExtraTurn() {
        enableAutoStop();
        harness.setHand(player1, List.of(new TimeWarp()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameData gd = harness.getGameData();
        int turnBefore = gd.turnNumber;

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        // End current turn → extra turn (player1)
        advanceTurn();
        // End extra turn → normal turn (player2)
        advanceTurn();

        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.turnNumber).isEqualTo(turnBefore + 2);
        assertThat(gd.extraTurns).isEmpty();
    }

    // ===== Extra turn progression — targeting opponent =====

    @Test
    @DisplayName("Extra turn targeting opponent gives them the extra turn")
    void extraTurnTargetingOpponent() {
        enableAutoStop();
        harness.setHand(player1, List.of(new TimeWarp()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameData gd = harness.getGameData();
        int turnBefore = gd.turnNumber;

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // End current turn → extra turn for player2
        advanceTurn();
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.turnNumber).isEqualTo(turnBefore + 1);

        // End extra turn → normal alternation (player1's turn would have been next,
        // but player2 just took extra turn so normal order resumes)
        advanceTurn();
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.turnNumber).isEqualTo(turnBefore + 2);
    }
}
