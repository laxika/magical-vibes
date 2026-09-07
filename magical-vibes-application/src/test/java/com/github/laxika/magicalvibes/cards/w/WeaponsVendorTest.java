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
        harness.setHand(player1, List.of(new WeaponsVendor()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
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

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(equipment.getId());
        harness.handlePermanentChosen(player1, equipment.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(vendor.getId(), firstCreature.getId(), secondCreature.getId());
        harness.handlePermanentChosen(player1, secondCreature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(equipment.getAttachedTo()).isEqualTo(secondCreature.getId());
    }

    @Test
    @DisplayName("Declining the payment leaves the Equipment unattached")
    void decliningPaymentDoesNotAttachEquipment() {
        addVendorReady();
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player1);

        harness.handlePermanentChosen(player1, equipment.getId());
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(equipment.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("The combat ability is not offered without a controlled Equipment")
    void noEquipmentMeansNoCombatAbility() {
        addVendorReady();

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addVendorReady() {
        WeaponsVendor vendor = new WeaponsVendor();
        Permanent permanent = new Permanent(vendor);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
