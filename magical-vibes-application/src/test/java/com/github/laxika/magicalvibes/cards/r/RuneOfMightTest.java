package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DarksteelPlate;
import com.github.laxika.magicalvibes.cards.b.BrassSquire;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MerfolkTrickster;
import com.github.laxika.magicalvibes.cards.t.TheBlackstaffOfWaterdeep;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RuneOfMight.class, GrizzlyBears.class, DarksteelPlate.class, BrassSquire.class,
        MerfolkTrickster.class, TheBlackstaffOfWaterdeep.class})
class RuneOfMightTest extends BaseCardTest {

    @Test
    @DisplayName("Rune enters attached to any permanent and draws a card")
    void entersAttachedAndDraws() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new RuneOfMight()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Rune of Might")
                        && bears.getId().equals(p.getAttachedTo()));
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Rune grants +1/+1 and trample to an enchanted creature")
    void enchantedCreatureGetsBoostAndTrample() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent rune = harness.addToBattlefieldAndReturn(player1, new RuneOfMight());
        rune.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Rune grants +1/+1 and trample through an enchanted Equipment")
    void enchantedEquipmentBoostsEquippedCreature() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new DarksteelPlate());
        equipment.setAttachedTo(firstCreature.getId());
        Permanent rune = harness.addToBattlefieldAndReturn(player1, new RuneOfMight());
        rune.setAttachedTo(equipment.getId());

        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, firstCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, firstCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.TRAMPLE)).isFalse();

        equipment.setAttachedTo(secondCreature.getId());

        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, firstCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, secondCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, firstCreature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Rune does not boost an unattached Equipment")
    void unattachedEquipmentDoesNotReceiveEquipmentBranch() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new DarksteelPlate());
        Permanent rune = harness.addToBattlefieldAndReturn(player1, new RuneOfMight());
        rune.setAttachedTo(equipment.getId());

        assertThat(gqs.getEffectivePower(gd, equipment)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, equipment)).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, equipment, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Enchanting an opponent's noncreature permanent draws for the Rune's controller")
    void enchantingOpposingEquipmentDrawsForAuraController() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new DarksteelPlate());
        harness.setHand(player1, List.of(new RuneOfMight()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, equipment.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent rune = findPermanent(player1, "Rune of Might");
        assertThat(rune.getAttachedTo()).isEqualTo(equipment.getId());
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gqs.hasKeyword(gd, equipment, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Rune benefits an opponent's equipped creature and stops when the Rune leaves")
    void opposingEquipmentBenefitsItsWearerUntilRuneLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new DarksteelPlate());
        equipment.setAttachedTo(creature.getId());
        Permanent rune = harness.addToBattlefieldAndReturn(player1, new RuneOfMight());
        rune.setAttachedTo(equipment.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(rune);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("An Equipment that lost its abilities cannot grant the Rune's bonus when attached")
    void equipmentAbilityRemovalSuppressesGrantedBonus() {
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new TheBlackstaffOfWaterdeep());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new DarksteelPlate());
        Permanent squire = harness.addToBattlefieldAndReturn(player1, new BrassSquire());
        squire.setSummoningSick(false);
        Permanent rune = harness.addToBattlefieldAndReturn(player1, new RuneOfMight());
        rune.setAttachedTo(equipment.getId());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, equipment.getId());
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, equipment)).isTrue();
        assertThat(gqs.getEffectivePower(gd, equipment)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, equipment, Keyword.TRAMPLE)).isTrue();

        harness.setHand(player2, List.of(new MerfolkTrickster()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castCreature(player2, 0, 0, equipment.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        staff.setTapped(false);
        harness.runStateBasedActions();
        assertThat(gqs.isCreature(gd, equipment)).isFalse();
        harness.activateAbilityWithMultiTargets(player1, 2, 0,
                List.of(equipment.getId(), squire.getId()));
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(squire.getId());
        assertThat(gqs.getEffectivePower(gd, squire)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, squire)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, squire, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Rune does not enter or draw if its target leaves before resolution")
    void illegalTargetPreventsEnteringAndDrawing() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RuneOfMight()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Rune of Might");
        harness.assertInGraveyard(player1, "Rune of Might");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }
}
