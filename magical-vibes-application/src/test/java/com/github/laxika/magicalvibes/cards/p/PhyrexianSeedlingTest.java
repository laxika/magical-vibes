package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhyrexianSeedling.class, GrizzlyBears.class})
class PhyrexianSeedlingTest extends BaseCardTest {

    @Test
    void entersWithOnePlusOneCounter() {
        harness.setHand(player1, List.of(new PhyrexianSeedling()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent seedling = findPermanent(player1, "Phyrexian Seedling");
        assertThat(seedling.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(seedling.getEffectivePower()).isEqualTo(1);
        assertThat(seedling.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void combatDamageProliferatesThatManyTimes() {
        Permanent seedling = addCreatureReady(player1, new PhyrexianSeedling());
        seedling.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        seedling.setAttacking(true);
        resolveCombat();
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }
}
