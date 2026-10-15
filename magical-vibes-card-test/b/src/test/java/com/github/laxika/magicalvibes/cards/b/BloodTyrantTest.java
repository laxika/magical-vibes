package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodTyrant.class})
class BloodTyrantTest extends BaseCardTest {

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
    @Test
    @DisplayName("Each Tyrant drains both players and receives only its own trigger's counters")
    void multipleTyrantsResolveIndependently() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BloodTyrant());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BloodTyrant());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Lethal upkeep life loss finishes resolving before the opponent loses the game")
    void lethalUpkeepFinishesBeforeGameEnds() {
        Permanent tyrant = harness.addToBattlefieldAndReturn(player1, new BloodTyrant());
        harness.setLife(player1, 20);
        harness.setLife(player2, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 0);
        assertThat(tyrant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }
}
