package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThunderingRaiju.class, GrizzlyBears.class})
class ThunderingRaijuTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking puts a +1/+1 counter on a creature you control and damages each opponent")
    void attackTriggerCountersTargetAndDealsDamageForOtherModifiedCreatures() {
        harness.setLife(player2, 20);

        Permanent raiju = addCreatureReady(player1, new ThunderingRaiju());
        raiju.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherModifiedCreature = addCreatureReady(player1, new GrizzlyBears());
        otherModifiedCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }
}
