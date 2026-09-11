package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CentaurCourser;
import com.github.laxika.magicalvibes.cards.g.GiantTortoise;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HulklingBurgeoningBruiser.class, CentaurCourser.class, GiantTortoise.class,
        GrizzlyBears.class})
class HulklingBurgeoningBruiserTest extends BaseCardTest {

    @Test
    void putsCounterWhenEnteringCreatureHasGreaterPower() {
        Permanent hulkling = addHulkling();

        castCreature(new CentaurCourser(), ManaColor.GREEN, ManaColor.COLORLESS, ManaColor.COLORLESS);

        assertThat(hulkling.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void putsCounterWhenEnteringCreatureHasGreaterToughness() {
        Permanent hulkling = addHulkling();

        castCreature(new GiantTortoise(), ManaColor.BLUE, ManaColor.COLORLESS);

        assertThat(hulkling.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerWhenNeitherCharacteristicIsGreater() {
        Permanent hulkling = addHulkling();

        castCreature(new GrizzlyBears(), ManaColor.GREEN, ManaColor.COLORLESS);

        assertThat(hulkling.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void checksTheComparisonAgainWhenTheAbilityResolves() {
        Permanent hulkling = addHulkling();

        harness.setHand(player1, List.of(new CentaurCourser()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        hulkling.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(hulkling.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addHulkling() {
        return harness.addToBattlefieldAndReturn(player1, new HulklingBurgeoningBruiser());
    }

    private void castCreature(Card creature, ManaColor... mana) {
        harness.setHand(player1, List.of(creature));
        for (ManaColor color : mana) {
            harness.addMana(player1, color, 1);
        }
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
