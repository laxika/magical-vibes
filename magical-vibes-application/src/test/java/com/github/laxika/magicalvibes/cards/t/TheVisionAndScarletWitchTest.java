package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheVisionAndScarletWitch.class, GrizzlyBears.class})
class TheVisionAndScarletWitchTest extends BaseCardTest {

    @Test
    void castingASpellAddsRedManaAndAPlusOnePlusOneCounter() {
        Permanent visionAndScarletWitch = addCreatureReady(player1, new TheVisionAndScarletWitch());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        ManaPool manaPool = gd.playerManaPools.get(player1.getId());
        assertThat(manaPool.get(ManaColor.RED)).isEqualTo(1);
        assertThat(visionAndScarletWitch.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(visionAndScarletWitch.getEffectivePower()).isEqualTo(4);
        assertThat(visionAndScarletWitch.getEffectiveToughness()).isEqualTo(4);
    }
}
