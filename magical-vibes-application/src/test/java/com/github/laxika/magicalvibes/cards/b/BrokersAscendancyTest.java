package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrokersAscendancy.class, ChandraNalaar.class, GrizzlyBears.class})
class BrokersAscendancyTest extends BaseCardTest {

    @Test
    void putsCountersOnControlledCreaturesAndPlaneswalkersAtYourEndStep() {
        Permanent ascendancy = harness.addToBattlefieldAndReturn(player1, new BrokersAscendancy());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownPlaneswalker = harness.addToBattlefieldAndReturn(player1, new ChandraNalaar());
        ownPlaneswalker.setCounterCount(CounterType.LOYALTY, 3);
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opposingPlaneswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        opposingPlaneswalker.setCounterCount(CounterType.LOYALTY, 3);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opposingPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(ascendancy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ascendancy.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
