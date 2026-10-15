package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.Manalith;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DivineFavor.class, RuneclawBear.class, Manalith.class, Naturalize.class})
class DivineFavorTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Divine Favor attaches it and gains 3 life")
    void resolvingAttachesAndGainsLife() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());

        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.setHand(player1, List.of(new DivineFavor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Divine Favor")
                        && bears.getId().equals(p.getAttachedTo()));
        harness.assertLife(player1, startingLife + 3);
    }

    @Test
    @DisplayName("Enchanted creature gets +1/+3")
    void enchantedCreatureGetsBoost() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DivineFavor());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    @DisplayName("Creature returns to base stats when Divine Favor is removed")
    void boostStopsWhenRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DivineFavor());
        aura.setAttachedTo(bears.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Divine Favor fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());

        harness.setHand(player1, List.of(new DivineFavor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bears);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Divine Favor");
        harness.assertNotOnBattlefield(player1, "Divine Favor");
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new Manalith());
        harness.setHand(player1, List.of(new DivineFavor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent artifact = findPermanent(player1, "Manalith");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Enchanting an opponent's creature boosts it but gives life only to the Aura controller")
    void enchantsOpponentCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.setLife(player1, 10);
        harness.setLife(player2, 12);
        harness.setHand(player1, List.of(new DivineFavor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Divine Favor").getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
        harness.assertLife(player1, 13);
        harness.assertLife(player2, 12);
    }

    @Test
    @DisplayName("Life gain uses the stack and resolves even after the Aura is destroyed")
    void lifeGainResolvesAfterAuraDestroyed() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new DivineFavor(), new Naturalize()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        Permanent aura = findPermanent(player1, "Divine Favor");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);

        harness.castInstant(player1, 0, aura.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Divine Favor");
        harness.assertLife(player1, 10);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);

        resolveAllTriggers();

        harness.assertLife(player1, 13);
    }
}
