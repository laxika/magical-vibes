package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RampagingAetherhood.class)
class RampagingAetherhoodTest extends BaseCardTest {

    @Test
    void gainsEnergyEqualToPowerThenAddsCountersFromPayment() {
        Permanent aetherhood = harness.addToBattlefieldAndReturn(player1, new RampagingAetherhood());

        advanceToUpkeep(player1);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(4);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);
        harness.handleXValueChosen(player1, 2);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(aetherhood.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(aetherhood.getEffectivePower()).isEqualTo(6);
    }

    @Test
    void mayDeclineTheEnergyPayment() {
        Permanent aetherhood = harness.addToBattlefieldAndReturn(player1, new RampagingAetherhood());

        advanceToUpkeep(player1);
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(4);
        assertThat(aetherhood.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
