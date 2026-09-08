package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LightOfPromiseTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets counters equal to life gained")
    void putsCountersEqualToLifeGained() {
        Permanent host = addCreatureReady(player1, new GrizzlyBears());
        enchantHost(host);

        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve Angel of Mercy and its ETB trigger
        harness.passBothPriorities(); // resolve the 3-life gain
        harness.passBothPriorities(); // resolve Light of Promise's trigger

        assertThat(host.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    private void enchantHost(Permanent host) {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new LightOfPromise()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0, host.getId());
        harness.passBothPriorities();
    }
}
