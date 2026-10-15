package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.f.FearlessLiberator;
import com.github.laxika.magicalvibes.cards.k.KondasBanner;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.m.MaskwoodNexus;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArmedAndArmored.class, DuskLegionDreadnought.class, FearlessLiberator.class,
        KondasBanner.class, LeoninScimitar.class, MaskwoodNexus.class})
class ArmedAndArmoredTest extends BaseCardTest {

    @Test
    @DisplayName("Animates own Vehicles and chooses a controlled Dwarf before attaching Equipment")
    void animatesVehiclesChoosesDwarfAndAttachesEquipment() {
        Permanent ownVehicle = harness.addToBattlefieldAndReturn(player1, new DuskLegionDreadnought());
        Permanent opponentVehicle = harness.addToBattlefieldAndReturn(player2, new DuskLegionDreadnought());
        Permanent ownDwarf = addCreatureReady(player1, new FearlessLiberator());
        Permanent opponentDwarf = addCreatureReady(player2, new FearlessLiberator());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent illegalEquipment = harness.addToBattlefieldAndReturn(player1, new KondasBanner());

        cast();

        assertThat(gqs.isCreature(gd, ownVehicle)).isTrue();
        assertThat(ownVehicle.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(gqs.isCreature(gd, opponentVehicle)).isFalse();

        PendingInteraction.PermanentChoice dwarfChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(dwarfChoice.validIds()).contains(ownDwarf.getId()).doesNotContain(opponentDwarf.getId());
        harness.handlePermanentChosen(player1, ownDwarf.getId());

        PendingInteraction.MultiPermanentChoice equipmentChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(equipmentChoice.validIds()).containsExactly(equipment.getId())
                .doesNotContain(illegalEquipment.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(equipment.getId()));

        assertThat(equipment.getAttachedTo()).isEqualTo(ownDwarf.getId());
        assertThat(illegalEquipment.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Vehicle animation and attachments expire or remain unchanged correctly")
    void vehicleAnimationExpiresAtEndOfTurn() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new DuskLegionDreadnought());
        Permanent dwarf = addCreatureReady(player1, new FearlessLiberator());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        cast();
        harness.handlePermanentChosen(player1, dwarf.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(equipment.getId()));

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
        assertThat(equipment.getAttachedTo()).isEqualTo(dwarf.getId());
    }

    @Test
    @DisplayName("Animates Vehicles even when no controlled Dwarf can be chosen")
    void animatesWithoutControlledDwarf() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new DuskLegionDreadnought());
        addCreatureReady(player2, new FearlessLiberator());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        cast();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Choosing zero Equipment preserves existing attachments")
    void canChooseZeroEquipment() {
        Permanent dwarf = addCreatureReady(player1, new FearlessLiberator());
        Permanent otherDwarf = addCreatureReady(player1, new FearlessLiberator());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        equipment.setAttachedTo(otherDwarf.getId());

        cast();
        harness.handlePermanentChosen(player1, dwarf.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(equipment.getAttachedTo()).isEqualTo(otherDwarf.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Moves multiple controlled Equipment and excludes opposing Equipment")
    void attachesMultipleEquipment() {
        Permanent dwarf = addCreatureReady(player1, new FearlessLiberator());
        Permanent otherDwarf = addCreatureReady(player1, new FearlessLiberator());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        first.setAttachedTo(otherDwarf.getId());

        cast();
        harness.handlePermanentChosen(player1, dwarf.getId());
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId())
                .doesNotContain(opposing.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(first.getAttachedTo()).isEqualTo(dwarf.getId());
        assertThat(second.getAttachedTo()).isEqualTo(dwarf.getId());
        assertThat(opposing.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Does not animate Vehicles entering after resolution")
    void doesNotAnimateLaterVehicles() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new DuskLegionDreadnought());

        cast();
        Permanent later = harness.addToBattlefieldAndReturn(player1, new DuskLegionDreadnought());

        assertThat(gqs.isCreature(gd, original)).isTrue();
        assertThat(gqs.isCreature(gd, later)).isFalse();
    }

    @Test
    @DisplayName("Can choose an animated Vehicle made a Dwarf by Maskwood Nexus")
    void choosesDwarfGrantedByContinuousEffect() {
        harness.addToBattlefield(player1, new MaskwoodNexus());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new DuskLegionDreadnought());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        cast();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(vehicle.getId());
        harness.handlePermanentChosen(player1, vehicle.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(equipment.getId()));

        assertThat(equipment.getAttachedTo()).isEqualTo(vehicle.getId());
    }

    private void cast() {
        harness.setHand(player1, List.of(new ArmedAndArmored()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);
    }
}
