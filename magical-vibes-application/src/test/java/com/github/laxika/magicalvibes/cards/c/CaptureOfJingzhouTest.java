package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

class CaptureOfJingzhouTest extends BaseCardTest {

    /** Stops auto-pass at PRECOMBAT_MAIN for both players so turns advance one at a time. */
    private void enableAutoStop() {
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

    private void cast() {
        harness.setHand(player1, List.of(new CaptureOfJingzhou()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Resolving queues one extra turn for the caster")
    void resolvingQueuesOneExtraTurn() {
        enableAutoStop();
        cast();

        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }

    @Test
    @DisplayName("The extra turn is taken by the caster after the current turn ends")
    void extraTurnTakenByCaster() {
        enableAutoStop();
        int turnBefore = gd.turnNumber;
        cast();

        advanceTurn();

        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.turnNumber).isEqualTo(turnBefore + 1);
        assertThat(gd.extraTurns).isEmpty();
    }

    @Test
    @DisplayName("Normal turn order resumes after the single extra turn")
    void normalTurnOrderResumes() {
        enableAutoStop();
        int turnBefore = gd.turnNumber;
        cast();

        advanceTurn(); // extra turn
        advanceTurn(); // back to opponent

        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.turnNumber).isEqualTo(turnBefore + 2);
    }

    @Test
    @DisplayName("Capture of Jingzhou goes to the graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        cast();

        GameData g = harness.getGameData();
        assertThat(g.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Capture of Jingzhou"));
        assertThat(g.stack).isEmpty();
    }
}
