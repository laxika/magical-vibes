package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.ChromaticSphere;
import com.github.laxika.magicalvibes.cards.n.NomadicElf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Mourning.class, NomadicElf.class, ChromaticSphere.class})
class MourningTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets -2/-0")
    void enchantedCreatureGetsMinusTwoPower() {
        Permanent bears = addCreatureReady(player1, new NomadicElf());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Mourning());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Mourning only affects its enchanted creature")
    void onlyEnchantedCreatureGetsMinusTwoPower() {
        Permanent enchanted = addCreatureReady(player1, new NomadicElf());
        Permanent other = addCreatureReady(player1, new NomadicElf());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Mourning());
        aura.setAttachedTo(enchanted.getId());

        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(0);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
    }

    @Test
    @DisplayName("Activating {B} returns Mourning to its owner's hand")
    void activatedAbilityReturnsAuraToHand() {
        Permanent bears = addCreatureReady(player1, new NomadicElf());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Mourning());
        aura.setAttachedTo(bears.getId());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Mourning");
        harness.assertNotOnBattlefield(player1, "Mourning");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Resolving Mourning attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new NomadicElf());

        harness.setHand(player1, List.of(new Mourning()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Mourning")
                        && bears.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Mourning")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ChromaticSphere());
        harness.setHand(player1, List.of(new Mourning()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The Aura controller returns Mourning to its owner, not to themselves")
    void returnsToOwnerWhenControlledByOpponent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NomadicElf());
        Mourning mourning = new Mourning();
        mourning.setOwnerId(player2.getId());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, mourning);
        aura.setAttachedTo(creature.getId());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Mourning");
        harness.assertNotInHand(player1, "Mourning");
        harness.assertNotOnBattlefield(player1, "Mourning");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Two pending activations return Mourning only once")
    void multipleActivationsReturnOnlyOneCard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NomadicElf());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Mourning());
        aura.setAttachedTo(creature.getId());

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 1, null, null);
        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Mourning"))
                .hasSize(1);
        harness.assertNotOnBattlefield(player1, "Mourning");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Mourning cannot be returned without paying black mana")
    void activationRequiresBlackMana() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NomadicElf());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Mourning());
        aura.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Mourning");
        harness.assertNotInHand(player1, "Mourning");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(0);
    }
}
