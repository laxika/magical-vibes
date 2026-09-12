package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CinderSeer;
import com.github.laxika.magicalvibes.cards.t.ThranDynamo;
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

@CardUsed({MaskOfLawAndGrace.class, MetathranSoldier.class, ThranDynamo.class, CinderSeer.class})
class MaskOfLawAndGraceTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature has protection from black and red")
    void enchantedCreatureHasProtectionFromBlackAndRed() {
        Permanent creature = addCreatureReady(player1, new MetathranSoldier());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MaskOfLawAndGrace());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.RED)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature does not gain protection from other colors")
    void noProtectionFromOtherColors() {
        Permanent creature = addCreatureReady(player1, new MetathranSoldier());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MaskOfLawAndGrace());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.WHITE)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLUE)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.GREEN)).isFalse();
    }

    @Test
    @DisplayName("Only the enchanted creature gains protection")
    void protectionOnlyAppliesToEnchantedCreature() {
        Permanent enchantedCreature = addCreatureReady(player1, new MetathranSoldier());
        Permanent otherCreature = addCreatureReady(player1, new MetathranSoldier());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MaskOfLawAndGrace());
        aura.setAttachedTo(enchantedCreature.getId());

        assertThat(gqs.hasProtectionFrom(gd, enchantedCreature, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, enchantedCreature, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, otherCreature, CardColor.BLACK)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, otherCreature, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Protection is lost when Mask of Law and Grace leaves the battlefield")
    void protectionLostWhenRemoved() {
        Permanent creature = addCreatureReady(player1, new MetathranSoldier());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MaskOfLawAndGrace());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.RED)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLACK)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Resolving attaches Mask of Law and Grace and grants protection")
    void resolvesAndGrantsProtection() {
        Permanent creature = addCreatureReady(player2, new MetathranSoldier());
        MaskOfLawAndGrace mask = new MaskOfLawAndGrace();
        harness.setHand(player1, List.of(mask));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == mask
                        && creature.getId().equals(permanent.getAttachedTo()));
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.RED)).isTrue();
    }

    @Test
    @DisplayName("Protected creature cannot be targeted by a red ability")
    void protectedCreatureCannotBeTargetedByRedAbility() {
        Permanent creature = addCreatureReady(player1, new MetathranSoldier());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MaskOfLawAndGrace());
        aura.setAttachedTo(creature.getId());
        addCreatureReady(player2, new CinderSeer());
        harness.addMana(player2, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("Can enchant a creature")
    void canTargetCreature() {
        Permanent creature = addCreatureReady(player1, new MetathranSoldier());
        harness.setHand(player1, List.of(new MaskOfLawAndGrace()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ThranDynamo());
        harness.setHand(player1, List.of(new MaskOfLawAndGrace()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
