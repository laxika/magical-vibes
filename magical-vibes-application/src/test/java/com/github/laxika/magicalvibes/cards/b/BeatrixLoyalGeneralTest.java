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
    @DisplayName("A creature target is mandatory even when attaching Equipment will be declined")
    void mustChooseCreatureTargetBeforeDecliningAttachment() {
        addCreatureReady(player1, new BeatrixLoyalGeneral());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validIds()).contains(ownCreature.getId())
                .doesNotContain(opponentCreature.getId(), player1.getId());
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(equipment.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Attaching Equipment can be declined when the ability resolves")
    void canDeclineAttachmentAtResolution() {
        Permanent beatrix = addCreatureReady(player1, new BeatrixLoyalGeneral());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, beatrix.getId());
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT,
                () -> harness.handleMayAbilityChosen(player1, false));

        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A chosen subset moves from another creature while unchosen Equipment stays attached")
    void movesOnlyChosenEquipment() {
        Permanent beatrix = addCreatureReady(player1, new BeatrixLoyalGeneral());
        Permanent oldHost = addCreatureReady(player1, new GrizzlyBears());
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent unchosen = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        chosen.setAttachedTo(oldHost.getId());
        unchosen.setAttachedTo(oldHost.getId());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, beatrix.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId()));

        assertThat(chosen.getAttachedTo()).isEqualTo(beatrix.getId());
        assertThat(unchosen.getAttachedTo()).isEqualTo(oldHost.getId());
        assertThat(gqs.getEffectivePower(gd, oldHost)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, beatrix)).isEqualTo(5);
    }

    @Test
    @DisplayName("Equipment with a legendary attachment restriction can attach to Beatrix herself")
    void canAttachRestrictedEquipmentToBeatrix() {
        Permanent beatrix = addCreatureReady(player1, new BeatrixLoyalGeneral());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new KondasBanner());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, beatrix.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1, List.of(equipment.getId()));

        assertThat(equipment.getAttachedTo()).isEqualTo(beatrix.getId());
    }

    @Test
    @DisplayName("The ability resolves without an Equipment choice when no Equipment is controlled")
    void resolvesWithNoEquipment() {
        Permanent beatrix = addCreatureReady(player1, new BeatrixLoyalGeneral());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, beatrix.getId());
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT,
                () -> harness.handleMayAbilityChosen(player1, true));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Beatrix does not trigger at the beginning of an opponent's combat")
    void doesNotTriggerDuringOpponentsCombat() {
        addCreatureReady(player1, new BeatrixLoyalGeneral());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        advanceToCombat(player2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(equipment.getAttachedTo()).isNull();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
