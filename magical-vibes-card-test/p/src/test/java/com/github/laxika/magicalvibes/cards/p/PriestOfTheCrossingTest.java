package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PriestOfTheCrossing.class, GrizzlyBears.class})
class PriestOfTheCrossingTest extends BaseCardTest {

    @Test
    @DisplayName("Puts counters on your creatures for each creature you controlled that died")
    void putsCountersForControllerCreatureDeaths() {
        Permanent priest = addCreatureReady(player1, new PriestOfTheCrossing());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBear = addCreatureReady(player2, new GrizzlyBears());

        gd.creatureDeathCountThisTurn.put(player1.getId(), 2);
        gd.creatureDeathCountThisTurn.put(player2.getId(), 3);

        advanceToEndStepAndResolve(player2);

        assertThat(priest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opponentBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Puts no counters when no creature died this turn")
    void putsNoCountersWithoutCreatureDeaths() {
        Permanent priest = addCreatureReady(player1, new PriestOfTheCrossing());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        advanceToEndStepAndResolve(player2);

        assertThat(priest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void advanceToEndStepAndResolve(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
