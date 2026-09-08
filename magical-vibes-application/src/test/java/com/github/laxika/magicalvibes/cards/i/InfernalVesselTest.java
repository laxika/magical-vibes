package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InfernalVesselTest extends BaseCardTest {

    @Test
    @DisplayName("Returns from its first death with two +1/+1 counters as a Demon")
    void returnsAsDemonWithTwoCounters() {
        Permanent vessel = harness.addToBattlefieldAndReturn(player1, new InfernalVessel());

        kill(vessel);

        Permanent returned = findPermanent(player1, "Infernal Vessel");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(returned.getGrantedSubtypes()).contains(CardSubtype.DEMON);
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not return when the Demon dies")
    void doesNotReturnWhenDemonDies() {
        Permanent vessel = harness.addToBattlefieldAndReturn(player1, new InfernalVessel());

        kill(vessel);
        Permanent demon = findPermanent(player1, "Infernal Vessel");
        kill(demon);

        harness.assertNotOnBattlefield(player1, "Infernal Vessel");
        harness.assertInGraveyard(player1, "Infernal Vessel");
    }

    private void kill(Permanent vessel) {
        vessel.setMarkedDamage(vessel.getEffectiveToughness());
        harness.runStateBasedActions();
        harness.passBothPriorities();
    }
}
