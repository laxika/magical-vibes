package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.f.FearlessLiberator;
import com.github.laxika.magicalvibes.cards.k.KondasBanner;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
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
        KondasBanner.class, LeoninScimitar.class})
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

    private void cast() {
        harness.setHand(player1, List.of(new ArmedAndArmored()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
