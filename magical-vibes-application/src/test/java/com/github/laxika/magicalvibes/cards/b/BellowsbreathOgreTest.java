package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BellowsbreathOgre.class, GrizzlyBears.class})
class BellowsbreathOgreTest extends BaseCardTest {

    @Test
    void startsAtIntensityOneDealsThatMuchThenIntensifies() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent ogre = harness.enterBattlefieldAndReturn(player1, new BellowsbreathOgre());
        harness.passBothPriorities();

        assertThat(ogre.getCounterCount(CounterType.INTENSITY)).isEqualTo(1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(1));
            harness.handlePermanentChosen(player1, target.getId());
            harness.passBothPriorities();
        });

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(ogre.getCounterCount(CounterType.INTENSITY)).isEqualTo(2);
    }

    @Test
    void attackDamageUsesCurrentIntensity() {
        Permanent ogre = addCreatureReady(player1, new BellowsbreathOgre());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        ogre.setCounterCount(CounterType.INTENSITY, 3);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, target.getId());
            harness.passBothPriorities();
        });

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(ogre.getCounterCount(CounterType.INTENSITY)).isEqualTo(4);
    }
}
