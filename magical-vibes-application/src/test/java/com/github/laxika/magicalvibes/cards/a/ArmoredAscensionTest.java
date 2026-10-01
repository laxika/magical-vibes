package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArmoredAscension.class, Forest.class, Plains.class, RuneclawBear.class})
class ArmoredAscensionTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Armored Ascension puts it on the stack")
    void castingPutsOnStack() {
        Permanent bears = addCreatureReady(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new ArmoredAscension()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Armored Ascension");
    }

    @Test
    @DisplayName("Resolving Armored Ascension attaches it and grants +1/+1 per Plains and flying")
    void resolvesAndBoostsPerPlains() {
        Permanent bears = addCreatureReady(player1, new RuneclawBear());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());

        harness.setHand(player1, List.of(new ArmoredAscension()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Armored Ascension")
                        && bears.getId().equals(p.getAttachedTo()));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Armored Ascension updates dynamically when Plains count changes")
    void updatesDynamicallyWithPlainsCount() {
        Permanent bears = addCreatureReady(player1, new RuneclawBear());
        Permanent ascension = harness.addToBattlefieldAndReturn(player1, new ArmoredAscension());
        ascension.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);

        harness.addToBattlefield(player1, new Plains());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);

        harness.addToBattlefield(player1, new Plains());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Plains"));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Armored Ascension counts Plains but not other basic land types")
    void countsOnlyPlains() {
        Permanent bear = addCreatureReady(player1, new RuneclawBear());
        harness.addToBattlefield(player1, new Forest());
        Permanent ascension = harness.addToBattlefieldAndReturn(player1, new ArmoredAscension());
        ascension.setAttachedTo(bear.getId());

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);

        harness.addToBattlefield(player1, new Plains());

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
    }

    @Test
    @DisplayName("Armored Ascension counts Plains controlled by aura controller, even on opponent creature")
    void countsAurasControllerPlains() {
        Permanent opponentBears = addCreatureReady(player2, new RuneclawBear());

        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player2, new Plains());

        Permanent ascension = harness.addToBattlefieldAndReturn(player1, new ArmoredAscension());
        ascension.setAttachedTo(opponentBears.getId());

        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Armored Ascension grants flying to enchanted creature")
    void grantsFlying() {
        Permanent bears = addCreatureReady(player1, new RuneclawBear());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();

        Permanent ascension = harness.addToBattlefieldAndReturn(player1, new ArmoredAscension());
        ascension.setAttachedTo(bears.getId());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Armored Ascension effect ends when aura leaves battlefield")
    void effectEndsWhenAuraLeavesBattlefield() {
        Permanent bears = addCreatureReady(player1, new RuneclawBear());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());

        Permanent ascension = harness.addToBattlefieldAndReturn(player1, new ArmoredAscension());
        ascension.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(ascension);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Armored Ascension")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new Plains());
        harness.setHand(player1, List.of(new ArmoredAscension()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        Permanent land = findPermanent(player1, "Plains");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Armored Ascension fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemovedBeforeResolution() {
        Permanent bears = addCreatureReady(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new ArmoredAscension()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        gs.playCard(gd, player1, 0, 0, bears.getId(), null);
        gd.playerBattlefields.get(player1.getId()).remove(bears);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Armored Ascension");
        harness.assertNotOnBattlefield(player1, "Armored Ascension");
    }
}
