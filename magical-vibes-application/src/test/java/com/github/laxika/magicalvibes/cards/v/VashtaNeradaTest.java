package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(VashtaNerada.class)
class VashtaNeradaTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter at each end step when a creature died this turn")
    void getsCounterAtEachEndStepWhenCreatureDied() {
        Permanent vashtaNerada = harness.addToBattlefieldAndReturn(player1, new VashtaNerada());
        gd.creatureDeathCountThisTurn.put(player2.getId(), 1);

        advanceToEndStep(player2);
        harness.passBothPriorities();

        assertThat(vashtaNerada.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not get a counter when no creature died this turn")
    void doesNotGetCounterWithoutCreatureDeath() {
        Permanent vashtaNerada = harness.addToBattlefieldAndReturn(player1, new VashtaNerada());

        advanceToEndStep(player1);

        assertThat(vashtaNerada.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
