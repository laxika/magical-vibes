package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RunawayGrowth.class, Forest.class})
class RunawayGrowthTest extends BaseCardTest {

    @Test
    void startsAtIntensityOneWhenItEnters() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        RunawayGrowth growth = new RunawayGrowth();
        harness.setHand(player1, List.of(growth));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == growth)
                .findFirst()
                .orElseThrow();
        assertThat(aura.getCounterCount(CounterType.INTENSITY)).isEqualTo(1);
    }

    @Test
    void addsCurrentIntensityThenIntensifies() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new RunawayGrowth());
        aura.setAttachedTo(forest.getId());
        aura.setCounterCount(CounterType.INTENSITY, 3);

        harness.tapPermanent(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(4);
        assertThat(aura.getCounterCount(CounterType.INTENSITY)).isEqualTo(4);
    }
}
