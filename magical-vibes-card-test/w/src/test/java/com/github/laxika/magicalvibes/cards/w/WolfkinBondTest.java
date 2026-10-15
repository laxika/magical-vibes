package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.m.ManifoldKey;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WolfkinBond.class, GreenwoodSentinel.class, ManifoldKey.class, Disenchant.class, Unsummon.class})
class WolfkinBondTest extends BaseCardTest {

    private void addCastingMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    @Test
    @DisplayName("Resolving Wolfkin Bond creates a Wolf and boosts the enchanted creature")
    void resolvingCreatesWolfAndBoostsEnchantedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new WolfkinBond()));
        addCastingMana();

        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Wolfkin Bond")
                        && creature.getId().equals(permanent.getAttachedTo()));

        assertThat(countPermanents(player1, "Wolf")).isEqualTo(1);
        Permanent wolf = findPermanent(player1, "Wolf");
        assertThat(wolf.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(wolf.getCard().getSubtypes()).contains(CardSubtype.WOLF);
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
    }

    @Test
    @DisplayName("Wolfkin Bond stops boosting when it leaves the battlefield")
    void boostStopsWhenRemoved() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new WolfkinBond());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Wolfkin Bond cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent key = harness.addToBattlefieldAndReturn(player1, new ManifoldKey());
        harness.setHand(player1, List.of(new WolfkinBond()));
        addCastingMana();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, key.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void enchantingOpponentsCreatureCreatesWolfForAuraController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new WolfkinBond()));
        addCastingMana();

        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(findPermanent(player1, "Wolfkin Bond").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(countPermanents(player1, "Wolf")).isEqualTo(1);
        assertThat(countPermanents(player2, "Wolf")).isZero();
    }

    @Test
    void targetLeavingBeforeResolutionPreventsWolfCreation() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new WolfkinBond()));
        harness.setHand(player2, List.of(new Unsummon()));
        addCastingMana();
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.castAndResolveInstant(player2, 0, creature.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Greenwood Sentinel");
        harness.assertInGraveyard(player1, "Wolfkin Bond");
        harness.assertNotOnBattlefield(player1, "Wolfkin Bond");
        assertThat(countPermanents(player1, "Wolf")).isZero();
    }

    @Test
    void wolfTriggerResolvesAfterAuraIsDestroyed() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new WolfkinBond()));
        harness.setHand(player2, List.of(new Disenchant()));
        addCastingMana();
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Wolf")).isZero();
        Permanent aura = findPermanent(player1, "Wolfkin Bond");
        harness.castAndResolveInstant(player2, 0, aura.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Wolfkin Bond");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(countPermanents(player1, "Wolf")).isEqualTo(1);
    }
}
