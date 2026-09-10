package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Bulette.class)
class BuletteTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter at its controller's end step when a creature died this turn")
    void getsCounterWhenCreatureDied() {
        Permanent bulette = harness.addToBattlefieldAndReturn(player1, new Bulette());
        gd.creatureDeathCountThisTurn.put(player2.getId(), 1);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(bulette.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not get a counter when no creature died this turn")
    void doesNotGetCounterWithoutCreatureDeath() {
        Permanent bulette = harness.addToBattlefieldAndReturn(player1, new Bulette());

        advanceToEndStep(player1);

        assertThat(bulette.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger during an opponent's end step")
    void doesNotTriggerOnOpponentsEndStep() {
        Permanent bulette = harness.addToBattlefieldAndReturn(player1, new Bulette());
        gd.creatureDeathCountThisTurn.put(player2.getId(), 1);

        advanceToEndStep(player2);

        assertThat(bulette.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
