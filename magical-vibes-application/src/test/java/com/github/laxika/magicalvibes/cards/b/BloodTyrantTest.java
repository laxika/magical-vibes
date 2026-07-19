package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BloodTyrantTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    @Test
    @DisplayName("At the controller's upkeep each player loses 1 life and the Tyrant gets a +1/+1 counter per life lost")
    void upkeepDrainsEachPlayerAndAddsCounters() {
        Permanent tyrant = harness.addToBattlefieldAndReturn(player1, new BloodTyrant());
        int p1Before = gd.playerLifeTotals.get(player1.getId());
        int p2Before = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(p1Before - 1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(p2Before - 1);
        // Both players lost 1 life => two +1/+1 counters.
        assertThat(tyrant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        Permanent tyrant = harness.addToBattlefieldAndReturn(player1, new BloodTyrant());
        int p1Before = gd.playerLifeTotals.get(player1.getId());
        int p2Before = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2); // opponent's upkeep
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(p1Before);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(p2Before);
        assertThat(tyrant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Counters accumulate across multiple upkeeps")
    void countersAccumulateAcrossUpkeeps() {
        Permanent tyrant = harness.addToBattlefieldAndReturn(player1, new BloodTyrant());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve first upkeep trigger

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve second upkeep trigger

        assertThat(tyrant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }
}
