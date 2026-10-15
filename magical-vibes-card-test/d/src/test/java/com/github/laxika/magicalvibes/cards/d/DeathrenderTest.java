package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GoldmeadowDodger;
import com.github.laxika.magicalvibes.cards.g.GhostlyFlicker;
import com.github.laxika.magicalvibes.cards.c.CloudcrownOak;
import com.github.laxika.magicalvibes.cards.n.NamelessInversion;
import com.github.laxika.magicalvibes.cards.p.PlanarCleansing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Deathrender.class, GrizzlyBears.class, DoomBlade.class, AirElemental.class,
        GoldmeadowDodger.class, CloudcrownOak.class, NamelessInversion.class, GhostlyFlicker.class,
        PlanarCleansing.class})
class DeathrenderTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+2")
    void equippedCreatureGetsBoost() {
        Permanent deathrender = harness.addToBattlefieldAndReturn(player1, new Deathrender());

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        deathrender.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("When equipped creature dies, chosen creature enters with Deathrender attached")
    void deathTriggerPutsCreatureAndAttaches() {
        Permanent deathrender = harness.addToBattlefieldAndReturn(player1, new Deathrender());

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        deathrender.setAttachedTo(bears.getId());

        // Kill the equipped creature with Doom Blade; Air Elemental remains in hand to put.
        harness.setHand(player1, List.of(new DoomBlade(), new AirElemental()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities(); // Doom Blade resolves, bears dies, trigger goes on stack
        harness.passBothPriorities(); // trigger resolves, begins the hand-card choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);

        harness.handleCardChosen(player1, 0); // put Air Elemental

        Permanent airElemental = findPermanent(player1, "Air Elemental");
        assertThat(airElemental).isNotNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        Permanent deathrenderPerm = findPermanent(player1, "Deathrender");
        assertThat(deathrenderPerm.getAttachedTo()).isEqualTo(airElemental.getId());
    }

    @Test
    @DisplayName("Declining the death trigger leaves the creature in hand and Deathrender unattached")
    void deathTriggerDeclined() {
        Permanent deathrender = harness.addToBattlefieldAndReturn(player1, new Deathrender());

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        deathrender.setAttachedTo(bears.getId());

        harness.setHand(player1, List.of(new DoomBlade(), new AirElemental()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities(); // Doom Blade resolves, bears dies, trigger goes on stack
        harness.passBothPriorities(); // trigger resolves, begins the hand-card choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);

        harness.handleCardChosen(player1, -1); // decline

        harness.assertInHand(player1, "Air Elemental");
        Permanent deathrenderPerm = findPermanent(player1, "Deathrender");
        assertThat(deathrenderPerm.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Death trigger with no creature in hand does nothing")
    void deathTriggerNoCreatureInHand() {
        Permanent deathrender = harness.addToBattlefieldAndReturn(player1, new Deathrender());

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        deathrender.setAttachedTo(bears.getId());

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities(); // Doom Blade resolves, bears dies, trigger goes on stack
        harness.passBothPriorities(); // trigger resolves with no creature in hand â€” no choice

        assertThat(gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class)).isNull();
        Permanent deathrenderPerm = findPermanent(player1, "Deathrender");
        assertThat(deathrenderPerm.getAttachedTo()).isNull();
    }
    @Test
    @DisplayName("Equip costs two mana and moves the boost to the chosen creature")
    void equipMovesBoost() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Deathrender());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GoldmeadowDodger());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new CloudcrownOak());
        equipment.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(6);
    }

    @Test
    @DisplayName("The Equipment controller chooses from their hand when an opponent's equipped creature dies")
    void equipmentControllerUsesOwnHand() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Deathrender());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GoldmeadowDodger());
        equipment.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new NamelessInversion(), new CloudcrownOak()));
        harness.setHand(player2, List.of(new GoldmeadowDodger()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent replacement = findPermanent(player1, "Cloudcrown Oak");
        assertThat(replacement).isNotNull();
        assertThat(equipment.getAttachedTo()).isEqualTo(replacement.getId());
        harness.assertInHand(player2, "Goldmeadow Dodger");
    }

    @Test
    @DisplayName("Exiling the equipped creature does not trigger Deathrender")
    void exileDoesNotTrigger() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Deathrender());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GoldmeadowDodger());
        equipment.setAttachedTo(creature.getId());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new CloudcrownOak());
        harness.setHand(player1, List.of(new GhostlyFlicker(), new GoldmeadowDodger()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, List.of(creature.getId(), other.getId()));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(equipment.getAttachedTo()).isNull();
        harness.assertInHand(player1, "Goldmeadow Dodger");
    }

    @Test
    @DisplayName("A returned Deathrender is not attached by its previous object's death trigger")
    void blinkedEquipmentIsNotAttachedByOldTrigger() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Deathrender());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GoldmeadowDodger());
        equipment.setAttachedTo(creature.getId());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new CloudcrownOak());
        harness.setHand(player1, List.of(new NamelessInversion(), new GhostlyFlicker(), new GoldmeadowDodger()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castInstant(player1, 0, List.of(equipment.getId(), other.getId()));
        harness.passBothPriorities();
        Permanent returnedEquipment = findPermanent(player1, "Deathrender");
        assertThat(returnedEquipment.getId()).isNotEqualTo(equipment.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Goldmeadow Dodger");
        assertThat(returnedEquipment.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Deathrender triggers when destroyed simultaneously with its equipped creature")
    void simultaneousDestructionStillTriggers() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Deathrender());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GoldmeadowDodger());
        equipment.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new PlanarCleansing(), new CloudcrownOak()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Cloudcrown Oak");
        harness.assertInGraveyard(player1, "Deathrender");
    }
}
