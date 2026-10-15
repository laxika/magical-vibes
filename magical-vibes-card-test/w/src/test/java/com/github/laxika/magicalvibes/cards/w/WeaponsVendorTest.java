package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({WeaponsVendor.class, Forest.class, GrizzlyBears.class, LeoninScimitar.class})
class WeaponsVendorTest extends BaseCardTest {

    @Test
    @DisplayName("Weapons Vendor draws a card when it enters")
    void drawsCardWhenItEnters() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.castFromHand(player1, new WeaponsVendor(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Paying at the beginning of combat attaches a controlled Equipment to a controlled creature")
    void paysToAttachEquipmentAtBeginningOfCombat() {
        Permanent vendor = addVendorReady();
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(equipment.getId());
        harness.handlePermanentChosen(player1, equipment.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(vendor.getId(), firstCreature.getId(), secondCreature.getId());
        harness.handlePermanentChosen(player1, secondCreature.getId());
        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(secondCreature.getId());
    }

    @Test
    @DisplayName("Declining the payment leaves the Equipment unattached")
    void decliningPaymentDoesNotAttachEquipment() {
        addVendorReady();
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("The combat ability is not offered without a controlled Equipment")
    void noEquipmentMeansNoCombatAbility() {
        addVendorReady();

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The combat ability does not trigger during an opponent's turn")
    void noTriggerDuringOpponentsCombat() {
        addVendorReady();
        harness.addToBattlefield(player1, new LeoninScimitar());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An opponent's Equipment does not satisfy the combat condition")
    void opponentsEquipmentDoesNotEnableTrigger() {
        addVendorReady();
        harness.addToBattlefield(player2, new LeoninScimitar());

        advanceToCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Losing all Equipment before resolution prevents the payment offer")
    void equipmentConditionIsCheckedAgainOnResolution() {
        addVendorReady();
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        advanceToCombat(player1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(equipment);
        gd.playerGraveyards.get(player1.getId()).add(equipment.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Payment creates a separate trigger that can move attached Equipment and excludes opposing targets")
    void paymentCreatesSeparateTriggerToMoveEquipment() {
        Permanent vendor = addVendorReady();
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent previousCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        equipment.setAttachedTo(previousCreature.getId());
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.addToBattlefield(player2, new GrizzlyBears());
        advanceToCombat(player1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(equipment.getId());
        harness.handlePermanentChosen(player1, equipment.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(vendor.getId(), previousCreature.getId());
        harness.handlePermanentChosen(player1, vendor.getId());
        assertThat(equipment.getAttachedTo()).isEqualTo(previousCreature.getId());
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(vendor.getId());
    }

    private Permanent addVendorReady() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new WeaponsVendor());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
