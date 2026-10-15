package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThorinMountainKing.class, ColossalDreadmaw.class, GrizzlyBears.class, LeoninScimitar.class})
class ThorinMountainKingTest extends BaseCardTest {

    @Test
    @DisplayName("Thorin attaches all targeted Equipment and deals damage once after at least one attachment")
    void attachesEquipmentThenDealsDamageOnce() {
        Permanent host = addCreatureReady(player1, new GrizzlyBears());
        Permanent firstEquipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent secondEquipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent victim = addCreatureReady(player2, new ColossalDreadmaw());

        castThorin(List.of(host.getId(), firstEquipment.getId(), secondEquipment.getId()));
        resolveAllTriggers();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(victim.getId());
        assertThat(firstEquipment.getAttachedTo()).isEqualTo(host.getId());
        assertThat(secondEquipment.getAttachedTo()).isEqualTo(host.getId());

        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Thorin can choose no Equipment and does not create the damage ability")
    void canChooseNoEquipment() {
        Permanent host = addCreatureReady(player1, new GrizzlyBears());
        Permanent victim = addCreatureReady(player2, new ColossalDreadmaw());

        castThorin(List.of(host.getId()));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(victim.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Thorin can attach Equipment without choosing a damage target")
    void canDeclineDamageTarget() {
        Permanent host = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent victim = addCreatureReady(player2, new ColossalDreadmaw());

        castThorin(List.of(host.getId(), equipment.getId()));
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(equipment.getAttachedTo()).isEqualTo(host.getId());
        assertThat(victim.getMarkedDamage()).isZero();
        assertThat(host.getMarkedDamage()).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equipment already attached to the chosen creature does not trigger damage")
    void alreadyAttachedEquipmentDoesNotTriggerDamage() {
        Permanent host = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        equipment.setAttachedTo(host.getId());

        castThorin(List.of(host.getId(), equipment.getId()));
        resolveAllTriggers();

        assertThat(equipment.getAttachedTo()).isEqualTo(host.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(host.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Thorin moves Equipment and can damage a friendly creature")
    void movesEquipmentAndDamagesFriendlyCreature() {
        Permanent previousHost = addCreatureReady(player1, new GrizzlyBears());
        Permanent host = addCreatureReady(player1, new GrizzlyBears());
        Permanent victim = addCreatureReady(player1, new ColossalDreadmaw());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        equipment.setAttachedTo(previousHost.getId());

        castThorin(List.of(host.getId(), equipment.getId()));
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, victim.getId());
        resolveAllTriggers();

        assertThat(equipment.getAttachedTo()).isEqualTo(host.getId());
        assertThat(victim.getMarkedDamage()).isEqualTo(3);
        assertThat(previousHost.getMarkedDamage()).isZero();
    }
    @Test
    @DisplayName("Thorin still attaches legal Equipment when another Equipment target leaves")
    void ignoresEquipmentThatLeavesBeforeResolution() {
        Permanent host = addCreatureReady(player1, new GrizzlyBears());
        Permanent firstEquipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent secondEquipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent victim = addCreatureReady(player2, new ColossalDreadmaw());

        castThorin(List.of(host.getId(), firstEquipment.getId(), secondEquipment.getId()));
        harness.passBothPriorities();
        harness.getPermanentRemovalService().sacrificePermanentToGraveyard(gd, firstEquipment);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, victim.getId());
        resolveAllTriggers();

        assertThat(secondEquipment.getAttachedTo()).isEqualTo(host.getId());
        assertThat(victim.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Damage uses the equipped creature's last known power after it leaves")
    void damageUsesLastKnownPower() {
        Permanent host = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent victim = addCreatureReady(player2, new ColossalDreadmaw());

        castThorin(List.of(host.getId(), equipment.getId()));
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, victim.getId());
        harness.getPermanentRemovalService().sacrificePermanentToGraveyard(gd, host);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(victim.getMarkedDamage()).isEqualTo(3);
    }
    private void castThorin(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new ThorinMountainKing()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, targetIds);
    }
}
