package com.github.laxika.magicalvibes.cards.o;

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

@CardUsed({Ouroboroid.class, GrizzlyBears.class})
class OuroboroidTest extends BaseCardTest {

    @Test
    @DisplayName("Puts counters equal to its power on each creature you control at your combat")
    void putsCountersEqualToPowerOnControlledCreatures() {
        Permanent ouroboroid = addCreatureReady(player1, new Ouroboroid());
        ouroboroid.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(ouroboroid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger at the beginning of combat on an opponent's turn")
    void doesNotTriggerOnOpponentsCombat() {
        Permanent ouroboroid = addCreatureReady(player1, new Ouroboroid());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());

        advanceToCombat(player2);
        harness.passBothPriorities();

        assertThat(ouroboroid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
