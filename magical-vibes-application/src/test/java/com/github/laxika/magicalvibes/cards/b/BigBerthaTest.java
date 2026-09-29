package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BigBertha.class)
class BigBerthaTest extends BaseCardTest {

    @Test
    void entersWithXPlusOnePlusOneCounters() {
        harness.setHand(player1, List.of(new BigBertha()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, 3);
        harness.passBothPriorities();

        Permanent bertha = findPermanent(player1, "Big Bertha");
        assertThat(bertha.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(bertha.getEffectivePower()).isEqualTo(4);
        assertThat(bertha.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    void tapAbilityAddsPlusOnePlusOneCounter() {
        Permanent bertha = addCreatureReady(player1, new BigBertha());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bertha.isTapped()).isTrue();
        assertThat(bertha.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bertha.getEffectivePower()).isEqualTo(2);
        assertThat(bertha.getEffectiveToughness()).isEqualTo(2);
    }
}
