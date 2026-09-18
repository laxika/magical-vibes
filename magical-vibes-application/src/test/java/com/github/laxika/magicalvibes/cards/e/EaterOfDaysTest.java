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

    private void castEaterOfDays() {
        harness.castFromHand(player1, new EaterOfDays(), "{4}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
