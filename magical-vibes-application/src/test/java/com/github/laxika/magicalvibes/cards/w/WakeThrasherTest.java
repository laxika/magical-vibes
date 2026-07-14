package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class WakeThrasherTest extends BaseCardTest {

    @Test
    @DisplayName("A permanent you control untapping gives Wake Thrasher +1/+1")
    void anotherPermanentUntappingBoosts() {
        Permanent thrasher = harness.addToBattlefieldAndReturn(player1, new WakeThrasher());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.tap();

        runUntapStep(player1);
        resolveStack();

        assertThat(thrasher.getPowerModifier()).isEqualTo(1);
        assertThat(thrasher.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple permanents untapping in one untap step stack multiple boosts")
    void multipleUntapsStack() {
        Permanent thrasher = harness.addToBattlefieldAndReturn(player1, new WakeThrasher());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        thrasher.tap();
        bears.tap();

        // Both Wake Thrasher (itself) and the Grizzly Bears untap → two triggers.
        runUntapStep(player1);
        resolveStack();

        assertThat(thrasher.getPowerModifier()).isEqualTo(2);
        assertThat(thrasher.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtCleanup() {
        Permanent thrasher = harness.addToBattlefieldAndReturn(player1, new WakeThrasher());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.tap();

        runUntapStep(player1);
        resolveStack();
        assertThat(thrasher.getPowerModifier()).isEqualTo(1);

        // Advance into the cleanup step, which clears "until end of turn" boosts. Empty the hand so
        // cleanup doesn't pause on a discard-to-hand-size choice before it resets modifiers.
        harness.setHand(player1, new ArrayList<>());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(thrasher.getPowerModifier()).isEqualTo(0);
        assertThat(thrasher.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("A permanent an opponent controls untapping does not trigger Wake Thrasher")
    void opponentUntapDoesNotTrigger() {
        Permanent thrasher = harness.addToBattlefieldAndReturn(player1, new WakeThrasher());
        Permanent oppBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        oppBears.tap();

        runUntapStep(player2);
        resolveStack();

        assertThat(thrasher.getPowerModifier()).isEqualTo(0);
        assertThat(thrasher.getToughnessModifier()).isEqualTo(0);
    }

    /**
     * Advances from the opponent's turn into the given player's untap step so the engine actually
     * runs the untap (which is what fires the "becomes untapped" triggers).
     */
    private void runUntapStep(Player untappingPlayer) {
        Player opponent = untappingPlayer.equals(player1) ? player2 : player1;
        harness.forceActivePlayer(opponent);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn: untaps and enqueues the triggers
    }

    /** Resolves every triggered ability sitting on the stack. */
    private void resolveStack() {
        while (!gd.stack.isEmpty()) {
            harness.clearPriorityPassed();
            harness.passBothPriorities();
        }
    }
}
