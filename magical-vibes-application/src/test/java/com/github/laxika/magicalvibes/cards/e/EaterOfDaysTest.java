package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

    private void castEaterOfDays() {
        harness.setHand(player1, List.of(new EaterOfDays()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
