package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IllusionaryArmor.class, GrizzlyBears.class, Naturalize.class, Shock.class, IcyManipulator.class})
class IllusionaryArmorTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Illusionary Armor attaches it and gives the creature +4/+4")
    void castAttachesAndBoosts() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new IllusionaryArmor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Illusionary Armor")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bears.getId()));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(6);
    }

    @Test
    @DisplayName("Boost goes away when Illusionary Armor leaves the battlefield")
    void boostStopsWhenRemoved() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new IllusionaryArmor());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Illusionary Armor is sacrificed when the enchanted creature becomes the target of a spell")
    void sacrificedWhenEnchantedCreatureTargetedBySpell() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new IllusionaryArmor());
        aura.setAttachedTo(bears.getId());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bears.getId());

        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Illusionary Armor");
        harness.assertInGraveyard(player1, "Illusionary Armor");
    }

    @Test
    @DisplayName("Illusionary Armor is sacrificed when the enchanted creature becomes the target of an ability")
    void sacrificedWhenEnchantedCreatureTargetedByAbility() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new IllusionaryArmor());
        aura.setAttachedTo(bears.getId());

        Permanent icy = harness.addToBattlefieldAndReturn(player1, new IcyManipulator());
        icy.setSummoningSick(false);

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(icy), null, bears.getId());

        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Illusionary Armor");
        harness.assertInGraveyard(player1, "Illusionary Armor");
    }

    @Test
    @DisplayName("Targeting Illusionary Armor itself does not trigger the sacrifice")
    void doesNotTriggerWhenAuraItselfIsTargeted() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new IllusionaryArmor());
        aura.setAttachedTo(bears.getId());

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, aura.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Naturalize");
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new IcyManipulator());
        harness.setHand(player1, List.of(new IllusionaryArmor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        Permanent artifact = findPermanent(player1, "Icy Manipulator");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("An opponent's spell sacrifices the Aura before dealing damage to its creature")
    void opponentSpellSacrificesAuraBeforeDamage() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new IllusionaryArmor());
        aura.setAttachedTo(bears.getId());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bears.getId());

        assertThat(gd.stack).hasSize(2);
        harness.assertOnBattlefield(player1, "Illusionary Armor");
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(6);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Illusionary Armor");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Targeting another creature does not sacrifice Illusionary Armor")
    void targetingAnotherCreatureDoesNotTrigger() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new IllusionaryArmor());
        aura.setAttachedTo(enchanted.getId());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, other.getId());

        harness.assertOnBattlefield(player1, "Illusionary Armor");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(6);
    }

    @Test
    @DisplayName("A second Aura spell triggers the first Armor and then attaches normally")
    void auraSpellTriggersExistingArmor() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent firstArmor = harness.addToBattlefieldAndReturn(player1, new IllusionaryArmor());
        firstArmor.setAttachedTo(bears.getId());
        harness.setHand(player1, List.of(new IllusionaryArmor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0, bears.getId());
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Illusionary Armor");
        harness.assertInGraveyard(player1, "Illusionary Armor");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Illusionary Armor");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard() instanceof IllusionaryArmor)
                .singleElement().satisfies(p -> assertThat(p.getAttachedTo()).isEqualTo(bears.getId()));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(6);
    }
}
