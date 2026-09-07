package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KondasBanner;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BeatrixLoyalGeneral.class, GrizzlyBears.class, KondasBanner.class, LeoninScimitar.class})
class BeatrixLoyalGeneralTest extends BaseCardTest {

    @Test
    @DisplayName("Beginning of combat targets a creature you control and offers only your legal Equipment")
    void offersControlledEquipmentForChosenCreature() {
        addCreatureReady(player1, new BeatrixLoyalGeneral());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent firstEquipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent secondEquipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent illegalEquipment = harness.addToBattlefieldAndReturn(player1, new KondasBanner());
        Permanent opponentEquipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiPermanentChoice equipmentChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(equipmentChoice.validIds())
                .containsExactly(firstEquipment.getId(), secondEquipment.getId())
                .doesNotContain(illegalEquipment.getId())
                .doesNotContain(opponentEquipment.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(firstEquipment.getId(), secondEquipment.getId()));

        assertThat(firstEquipment.getAttachedTo()).isEqualTo(target.getId());
        assertThat(secondEquipment.getAttachedTo()).isEqualTo(target.getId());
        assertThat(opponentEquipment.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("The Equipment selection can choose none")
    void canChooseNoEquipment() {
        addCreatureReady(player1, new BeatrixLoyalGeneral());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(equipment.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("The target choice is optional and limited to creatures you control")
    void targetChoiceCanBeDeclined() {
        addCreatureReady(player1, new BeatrixLoyalGeneral());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validIds()).contains(ownCreature.getId()).doesNotContain(opponentCreature.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isNull();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
