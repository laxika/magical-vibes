package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(EaterOfDays.class)
class EaterOfDaysTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield queues two skips of its controller's next turns")
    void queuesTwoNextTurnSkips() {
        castEaterOfDays();

        assertThat(gd.skipNextTurnCount.getOrDefault(player1.getId(), 0)).isEqualTo(2);
        assertThat(gd.skipNextTurnCount.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Its controller's next two turns are skipped")
    void skipsNextTwoTurns() {
        castEaterOfDays();

        advanceTurn();
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());

        advanceTurn();
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.skipNextTurnCount.getOrDefault(player1.getId(), 0)).isEqualTo(1);

        advanceTurn();
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.skipNextTurnCount.getOrDefault(player1.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("The entering creature's controller gets the skips even when another player is active")
    void queuesSkipsForControllerNotActivePlayer() {
        harness.forceActivePlayer(player2);
        harness.enterBattlefieldAndReturn(player1, new EaterOfDays());
        harness.passBothPriorities();

        assertThat(gd.skipNextTurnCount.getOrDefault(player1.getId(), 0)).isEqualTo(2);
        assertThat(gd.skipNextTurnCount.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("The skip effect waits for the enter trigger to resolve")
    void skipsAreNotAppliedBeforeTriggerResolves() {
        harness.castFromHand(player1, new EaterOfDays(), "{4}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Eater of Days");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.skipNextTurnCount.getOrDefault(player1.getId(), 0)).isZero();

        harness.passBothPriorities();

        assertThat(gd.skipNextTurnCount.getOrDefault(player1.getId(), 0)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Two entries cause four turns to be skipped")
    void multipleEntriesAccumulateTurnSkips() {
        castEaterOfDays();
        castEaterOfDays();

        assertThat(gd.skipNextTurnCount.getOrDefault(player1.getId(), 0)).isEqualTo(4);

        advanceTurn();
        for (int remaining = 3; remaining >= 0; remaining--) {
            advanceTurn();
            assertThat(gd.activePlayerId).isEqualTo(player2.getId());
            assertThat(gd.skipNextTurnCount.getOrDefault(player1.getId(), 0)).isEqualTo(remaining);
        }

        advanceTurn();
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Normal turn order resumes after the next two turns have been skipped")
    void normalTurnsResumeAfterTwoSkips() {
        castEaterOfDays();

        advanceTurn();
        advanceTurn();
        advanceTurn();
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());

        advanceTurn();
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.skipNextTurnCount.getOrDefault(player1.getId(), 0)).isZero();

        advanceTurn();
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
    }

    private void castEaterOfDays() {
        harness.castFromHand(player1, new EaterOfDays(), "{4}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }
}
