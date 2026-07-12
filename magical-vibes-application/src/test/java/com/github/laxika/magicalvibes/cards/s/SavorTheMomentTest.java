package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

class SavorTheMomentTest extends BaseCardTest {

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
        harness.setHand(player1, List.of(new SavorTheMoment()));
        harness.addMana(player1, ManaColor.BLUE, 3);
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
    @DisplayName("Permanents stay tapped on the extra turn — its untap step is skipped")
    void untapStepSkippedOnExtraTurn() {
        enableAutoStop();
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear.tap();
        cast();

        advanceTurn(); // into the extra turn

        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(bear.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Summoning sickness still clears on the skipped extra turn so creatures can act")
    void summoningSicknessStillClears() {
        enableAutoStop();
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear.setSummoningSick(true);
        cast();

        advanceTurn(); // into the extra turn

        assertThat(bear.isSummoningSick()).isFalse();
    }

    @Test
    @DisplayName("Normal turn order (and untapping) resumes after the extra turn")
    void normalTurnOrderResumes() {
        enableAutoStop();
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear.tap();
        int turnBefore = gd.turnNumber;
        cast();

        advanceTurn(); // extra turn (untap skipped)
        advanceTurn(); // opponent's normal turn

        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.turnNumber).isEqualTo(turnBefore + 2);
    }
}
