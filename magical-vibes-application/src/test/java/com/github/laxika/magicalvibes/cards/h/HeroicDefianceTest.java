package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FleetfootPanther;
import com.github.laxika.magicalvibes.cards.k.KavuRecluse;
import com.github.laxika.magicalvibes.cards.m.MeteorCrater;
import com.github.laxika.magicalvibes.cards.s.ShiftingSky;
import com.github.laxika.magicalvibes.cards.s.Stratadon;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeroicDefiance.class, HonorableScout.class, KavuRecluse.class, MeteorCrater.class,
        FleetfootPanther.class, Stratadon.class, ShiftingSky.class})
class HeroicDefianceTest extends BaseCardTest {

    @Test
    @DisplayName("Heroic Defiance boosts an enchanted creature whose color is not most common")
    void boostsWhenEnchantedColorIsNotMostCommon() {
        Permanent kavu = addCreatureReady(player1, new KavuRecluse());
        addCreatureReady(player2, new HonorableScout());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HeroicDefiance());
        aura.setAttachedTo(kavu.getId());

        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(5);
    }

    @Test
    @DisplayName("Heroic Defiance does not boost a creature that shares a most common color")
    void doesNotBoostWhenEnchantedColorIsMostCommon() {
        Permanent scout = addCreatureReady(player1, new HonorableScout());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HeroicDefiance());
        aura.setAttachedTo(scout.getId());

        assertThat(gqs.getEffectivePower(gd, scout)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, scout)).isEqualTo(1);
    }

    @Test
    @DisplayName("Heroic Defiance does not boost a creature when its color is tied for most common")
    void doesNotBoostWhenEnchantedColorIsTiedForMostCommon() {
        Permanent kavu = addCreatureReady(player1, new KavuRecluse());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HeroicDefiance());
        aura.setAttachedTo(kavu.getId());

        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(2);
    }

    @Test
    @DisplayName("Heroic Defiance stops boosting when it leaves the battlefield")
    void boostEndsWhenAuraLeaves() {
        Permanent kavu = addCreatureReady(player1, new KavuRecluse());
        addCreatureReady(player2, new HonorableScout());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HeroicDefiance());
        aura.setAttachedTo(kavu.getId());

        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(5);
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(2);
    }

    @Test
    @DisplayName("Heroic Defiance does not boost a multicolored creature sharing a most common color")
    void doesNotBoostWhenMulticoloredCreatureSharesMostCommonColor() {
        Permanent panther = addCreatureReady(player1, new FleetfootPanther());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HeroicDefiance());
        aura.setAttachedTo(panther.getId());

        assertThat(gqs.getEffectivePower(gd, panther)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, panther)).isEqualTo(4);
    }

    @Test
    @DisplayName("Heroic Defiance boosts a colorless creature")
    void boostsColorlessCreature() {
        Permanent stratadon = addCreatureReady(player1, new Stratadon());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HeroicDefiance());
        aura.setAttachedTo(stratadon.getId());

        assertThat(gqs.getEffectivePower(gd, stratadon)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, stratadon)).isEqualTo(8);
    }

    @Test
    @DisplayName("Heroic Defiance updates when the most common color changes")
    void updatesWhenMostCommonColorChanges() {
        Permanent kavu = addCreatureReady(player1, new KavuRecluse());
        addCreatureReady(player2, new HonorableScout());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HeroicDefiance());
        aura.setAttachedTo(kavu.getId());

        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(5);

        addCreatureReady(player2, new KavuRecluse());

        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(2);
    }

    @Test
    @DisplayName("Heroic Defiance uses current colors changed by another permanent")
    void usesCurrentColorsChangedByAnotherPermanent() {
        harness.setHand(player1, List.of(new ShiftingSky()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardColor.WHITE.name());

        Permanent kavu = addCreatureReady(player1, new KavuRecluse());
        addCreatureReady(player2, new KavuRecluse());
        addCreatureReady(player2, new KavuRecluse());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HeroicDefiance());
        aura.setAttachedTo(kavu.getId());

        assertThat(gqs.getEffectiveColors(gd, kavu))
                .containsExactly(CardColor.WHITE);
        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(2);
    }

    @Test
    @DisplayName("Heroic Defiance cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new MeteorCrater());
        harness.setHand(player1, List.of(new HeroicDefiance()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
