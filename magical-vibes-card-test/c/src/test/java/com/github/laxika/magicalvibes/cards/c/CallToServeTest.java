package com.github.laxika.magicalvibes.cards.c;

import java.util.List;

import com.github.laxika.magicalvibes.cards.b.ButcherGhoul;
import com.github.laxika.magicalvibes.cards.m.MoorlandInquisitor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CallToServe.class, MoorlandInquisitor.class, ButcherGhoul.class, Cloudshift.class})
class CallToServeTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+2, flying, and is an Angel in addition to its other types")
    void enchantedCreatureGetsBoostFlyingAndAngel() {
        Permanent bears = addCreatureReady(player1, new MoorlandInquisitor());

        harness.setHand(player1, List.of(new CallToServe()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();

        assertThat(gqs.effectiveCreatureSubtypes(gd, bears))
                .contains(CardSubtype.ANGEL, CardSubtype.HUMAN, CardSubtype.SOLDIER);
    }

    @Test
    @DisplayName("Removing Call to Serve removes the boost, flying, and Angel type")
    void effectsStopWhenRemoved() {
        Permanent bears = addCreatureReady(player1, new MoorlandInquisitor());

        harness.setHand(player1, List.of(new CallToServe()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Call to Serve");
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.ANGEL)).isFalse();
    }

    @Test
    @DisplayName("Cannot enchant a black creature")
    void cannotEnchantBlackCreature() {
        Permanent imp = addCreatureReady(player2, new ButcherGhoul());
        addCreatureReady(player1, new MoorlandInquisitor());

        harness.setHand(player1, List.of(new CallToServe()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, imp.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonblack creature");
    }

    @Test
    void canEnchantOpponentsCreatureWithoutAffectingOtherCreatures() {
        Permanent target = addCreatureReady(player2, new MoorlandInquisitor());
        Permanent other = addCreatureReady(player1, new MoorlandInquisitor());
        harness.setHand(player1, List.of(new CallToServe()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, target))
                .contains(CardSubtype.ANGEL, CardSubtype.HUMAN, CardSubtype.SOLDIER);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, other, CardSubtype.ANGEL)).isFalse();
    }

    @Test
    void cannotEnchantNoncreaturePermanent() {
        Permanent target = addCreatureReady(player1, new MoorlandInquisitor());
        harness.setHand(player1, List.of(new CallToServe(), new CallToServe()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        Permanent aura = findPermanent(player1, "Call to Serve");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, aura.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonblack creature");
    }

    @Test
    void doesNotResolveWhenTargetLeavesAndReturns() {
        Permanent target = addCreatureReady(player1, new MoorlandInquisitor());
        harness.setHand(player1, List.of(new CallToServe(), new Cloudshift()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Call to Serve");
        harness.assertNotOnBattlefield(player1, "Call to Serve");
        Permanent returned = findPermanent(player1, "Moorland Inquisitor");
        assertThat(returned.getId()).isNotEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, returned, CardSubtype.ANGEL)).isFalse();
    }

    @Test
    void auraGoesToGraveyardWhenEnchantedCreatureIsFlickered() {
        Permanent target = addCreatureReady(player1, new MoorlandInquisitor());
        harness.setHand(player1, List.of(new CallToServe(), new Cloudshift()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player1, "Call to Serve");
        harness.assertNotOnBattlefield(player1, "Call to Serve");
        Permanent returned = findPermanent(player1, "Moorland Inquisitor");
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, returned, CardSubtype.ANGEL)).isFalse();
    }
}
