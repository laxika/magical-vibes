package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ProdigiousGrowth.class, GrizzlyBears.class, FountainOfYouth.class})
class ProdigiousGrowthTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +7/+7 and trample")
    void enchantedCreatureGetsBoostAndTrample() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent growth = harness.addToBattlefieldAndReturn(player1, new ProdigiousGrowth());
        growth.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(9);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature loses the boost and trample when Prodigious Growth is removed")
    void effectsStopWhenRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent growth = harness.addToBattlefieldAndReturn(player1, new ProdigiousGrowth());
        growth.setAttachedTo(bears.getId());

        gd.playerBattlefields.get(player1.getId()).remove(growth);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Prodigious Growth")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new ProdigiousGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Resolves attached to an opponent's creature and affects only that creature")
    void resolvesOnOpponentsCreature() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ProdigiousGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();

        Permanent growth = findPermanent(player1, "Prodigious Growth");
        assertThat(growth.getAttachedTo()).isEqualTo(enchanted.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(enchanted);
        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(9);
        assertThat(gqs.hasKeyword(gd, enchanted, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, other, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Multiple copies give cumulative boosts and trample remains when one is removed")
    void multipleCopiesStack() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ProdigiousGrowth(), new ProdigiousGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 12);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Prodigious Growth")).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(16);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(16);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Prodigious Growth"));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(9);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Aura goes to the graveyard if its target leaves before resolution")
    void targetLeavesBeforeResolution() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ProdigiousGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castEnchantment(player1, 0, bears.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bears);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Prodigious Growth");
        harness.assertInGraveyard(player1, "Prodigious Growth");
    }

    @Test
    @DisplayName("Aura goes to the graveyard when its enchanted creature leaves")
    void enchantedCreatureLeaves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ProdigiousGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(bears);
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Prodigious Growth");
        harness.assertInGraveyard(player1, "Prodigious Growth");
    }
}
