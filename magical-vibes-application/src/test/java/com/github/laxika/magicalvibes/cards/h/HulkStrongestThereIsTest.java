package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AbominationIrradiatedBrute;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HulkStrongestThereIs.class, AbominationIrradiatedBrute.class, GrizzlyBears.class})
class HulkStrongestThereIsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with a +1/+1 counter")
    void entersWithCounter() {
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player1, List.of(new HulkStrongestThereIs()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent hulk = gd.playerBattlefields.get(player1.getId()).get(0);
        assertThat(hulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
    }

    @Test
    @DisplayName("Upkeep trigger doubles +1/+1 counters only on Gamma creatures you control")
    void upkeepTriggerDoublesGammaCounters() {
        Permanent hulk = addCreatureReady(player1, new HulkStrongestThereIs());
        hulk.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        hulk.setCounterCount(CounterType.CHARGE, 4);

        Permanent gamma = addCreatureReady(player1, new AbominationIrradiatedBrute());
        gamma.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent nonGamma = addCreatureReady(player1, new GrizzlyBears());
        nonGamma.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        Permanent opposingGamma = addCreatureReady(player2, new AbominationIrradiatedBrute());
        opposingGamma.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 7);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(hulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(hulk.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
        assertThat(gamma.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(nonGamma.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(opposingGamma.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
    }
}
