package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IronHillsStalwart.class, GrizzlyBears.class, LeoninScimitar.class})
class IronHillsStalwartTest extends BaseCardTest {

    @Test
    @DisplayName("When Iron Hills Stalwart enters, it can attach a controlled Equipment to a controlled creature")
    void enteringAttachesEquipmentToControlledCreature() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castStalwart();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("When Iron Hills Stalwart enters, its optional creature target can be declined")
    void enteringCanDeclineCreatureTarget() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        castStalwart();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, equipment.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Iron Hills Stalwart's ability is not put on the stack without a controlled Equipment")
    void noControlledEquipmentMeansNoAbility() {
        castStalwart();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void enteringCanAttachEquipmentToItself() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        castStalwart();
        harness.passBothPriorities();
        Permanent stalwart = findPermanent(player1, "Iron Hills Stalwart");
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.handlePermanentChosen(player1, stalwart.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(stalwart.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void enteringMovesAlreadyAttachedEquipment() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent originalCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        equipment.setAttachedTo(originalCreature.getId());

        castStalwart();
        harness.passBothPriorities();
        Permanent stalwart = findPermanent(player1, "Iron Hills Stalwart");
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.handlePermanentChosen(player1, stalwart.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(stalwart.getId());
    }

    @Test
    void decliningCreatureTargetLeavesEquipmentAttached() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent originalCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        equipment.setAttachedTo(originalCreature.getId());

        castStalwart();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(originalCreature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentEquipmentCannotBeChosen() {
        harness.addToBattlefield(player2, new LeoninScimitar());

        castStalwart();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentCreatureCannotBeChosen() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castStalwart();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, equipment.getId());

        var choice = (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        Permanent stalwart = findPermanent(player1, "Iron Hills Stalwart");
        assertThat(choice.validPermanentIds()).contains(stalwart.getId()).doesNotContain(opponentCreature.getId());
        harness.handlePermanentChosen(player1, stalwart.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(stalwart.getId());
    }

    @Test
    void creatureLosingControlBeforeResolutionPreventsAttachment() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castStalwart();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.handlePermanentChosen(player1, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void creatureLeavingBeforeResolutionLeavesEquipmentOnOriginalCreature() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent originalCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        equipment.setAttachedTo(originalCreature.getId());

        castStalwart();
        harness.passBothPriorities();
        Permanent stalwart = findPermanent(player1, "Iron Hills Stalwart");
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.handlePermanentChosen(player1, stalwart.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, stalwart));
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(originalCreature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipmentLeavingBeforeResolutionDoesNotAttach() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        castStalwart();
        harness.passBothPriorities();
        Permanent stalwart = findPermanent(player1, "Iron Hills Stalwart");
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.handlePermanentChosen(player1, stalwart.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, equipment));
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isNull();
        harness.assertInHand(player1, "Leonin Scimitar");
        assertThat(gd.stack).isEmpty();
    }

    private void castStalwart() {
        harness.castFromHand(player1, new IronHillsStalwart(), "{4}{R}");
    }
}
