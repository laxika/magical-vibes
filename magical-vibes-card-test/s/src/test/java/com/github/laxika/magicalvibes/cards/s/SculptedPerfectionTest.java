package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CopperHostCrusher;
import com.github.laxika.magicalvibes.cards.m.MaskwoodNexus;
import com.github.laxika.magicalvibes.cards.z.ZurEternalSchemer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SculptedPerfection.class, GrizzlyBears.class, CopperHostCrusher.class,
        MaskwoodNexus.class, ZurEternalSchemer.class})
class SculptedPerfectionTest extends BaseCardTest {

    @Test
    void entersWithIncubatorToken() {
        castSculptedPerfection();

        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void boostsTransformedIncubatorButNotNonPhyrexianCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castSculptedPerfection();

        Permanent incubator = findPermanent(player1, "Incubator");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(incubator), null, null);
        harness.passBothPriorities();

        assertThat(incubator.isTransformed()).isTrue();
        assertThat(gqs.getEffectivePower(gd, incubator)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, incubator)).isEqualTo(3);

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    private void castSculptedPerfection() {
        harness.castFromHand(player1, new SculptedPerfection(), "{2}{W}{B}");
        resolveAllTriggers();
    }

    @Test
    void incubatorStartsAsNoncreatureAndTransformsOnlyOnceForStackedActivations() {
        castSculptedPerfection();
        assertThat(countPermanents(player1, "Incubator")).isEqualTo(1);
        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(gqs.isCreature(gd, incubator)).isFalse();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(incubator);
        harness.activateAbility(player1, index, null, null);
        harness.activateAbility(player1, index, null, null);
        resolveAllTriggers();

        assertThat(incubator.isTransformed()).isTrue();
        assertThat(gqs.isCreature(gd, incubator)).isTrue();
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, incubator)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, incubator)).isEqualTo(3);
    }

    @Test
    void boostsOnlyOwnPhyrexiansAndBonusEndsWhenEnchantmentLeaves() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new CopperHostCrusher());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new CopperHostCrusher());
        castSculptedPerfection();

        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, own)).isEqualTo(9);
        assertThat(gqs.getEffectivePower(gd, opposing)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, opposing)).isEqualTo(8);

        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Sculpted Perfection"));
        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, own)).isEqualTo(8);
        assertThat(countPermanents(player1, "Incubator")).isEqualTo(1);
    }

    @Test
    void incubateTriggerResolvesAfterEnchantmentLeaves() {
        harness.castFromHand(player1, new SculptedPerfection(), "{2}{W}{B}");
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Incubator")).isZero();
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Sculpted Perfection"));
        resolveAllTriggers();

        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(incubator),
                null, null);
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, incubator)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, incubator)).isEqualTo(2);
    }

    @Test
    void boostsItselfWhenItBecomesAPhyrexianCreature() {
        Permanent zur = harness.addToBattlefieldAndReturn(player1, new ZurEternalSchemer());
        harness.addToBattlefield(player1, new MaskwoodNexus());
        castSculptedPerfection();
        Permanent perfection = findPermanent(player1, "Sculpted Perfection");
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(zur),
                null, perfection.getId());
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, perfection)).isTrue();
        assertThat(gqs.getEffectivePower(gd, perfection)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, perfection)).isEqualTo(5);
    }
}
