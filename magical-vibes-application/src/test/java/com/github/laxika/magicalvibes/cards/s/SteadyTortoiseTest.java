package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HarriedDash;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SteadyTortoise.class, HarriedDash.class, GrizzlyBears.class})
class SteadyTortoiseTest extends BaseCardTest {

    @Test
    void adventureCreatesHastyRabbit() {
        SteadyTortoise tortoise = new SteadyTortoise();
        harness.setHand(player1, List.of(tortoise));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        Permanent rabbit = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Rabbit"))
                .findFirst()
                .orElseThrow();
        assertThat(rabbit.getEffectivePower()).isEqualTo(1);
        assertThat(rabbit.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, rabbit, Keyword.HASTE)).isTrue();
    }

    @Test
    void attackTriggerPerpetuallyBoostsTortoiseOncePerCombat() {
        SteadyTortoise tortoise = new SteadyTortoise();
        Permanent tortoisePermanent = addCreatureReady(player1, tortoise);
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondBear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(bear),
                gd.playerBattlefields.get(player1.getId()).indexOf(secondBear)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, tortoisePermanent)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, tortoisePermanent)).isEqualTo(4);
    }

    @Test
    void exileAttackTriggerPerpetuallyBoostsTortoise() {
        SteadyTortoise tortoise = new SteadyTortoise();
        harness.setExile(player1, List.of(tortoise));
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(bear)));
        harness.passBothPriorities();

        assertThat(gd.perpetualCardPowerToughnessModifiers.get(tortoise.getId()).power()).isEqualTo(1);
        assertThat(gd.perpetualCardPowerToughnessModifiers.get(tortoise.getId()).toughness()).isEqualTo(1);
    }
}
