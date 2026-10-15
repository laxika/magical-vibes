package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CloudExSOLDIER.class, GrizzlyBears.class, LeoninScimitar.class})
class CloudExSOLDIERTest extends BaseCardTest {

    @Test
    @DisplayName("Attaches up to one target Equipment you control when Cloud enters")
    void attachesTargetEquipmentOnEntry() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent cloud = harness.enterBattlefieldAndReturn(player1, new CloudExSOLDIER());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(equipment.getId());

        harness.handlePermanentChosen(player1, equipment.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(cloud.getId());
    }

    @Test
    @DisplayName("Draws for each equipped attacking creature you control")
    void drawsForEachEquippedAttackingCreature() {
        Permanent cloud = addCreatureReady(player1, new CloudExSOLDIER());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonattackingBear = addCreatureReady(player1, new GrizzlyBears());
        attachEquipment(cloud);
        attachEquipment(bear);
        attachEquipment(nonattackingBear);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(player1, List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(cloud),
                gd.playerBattlefields.get(player1.getId()).indexOf(bear)));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Creates two Treasures when Cloud attacks with power 7 or greater")
    void createsTreasuresAtHighPower() {
        Permanent cloud = addCreatureReady(player1, new CloudExSOLDIER());
        attachEquipment(cloud);
        cloud.setPowerModifier(3);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(cloud)));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    @DisplayName("Can decline attaching Equipment even when a legal target exists")
    void canDeclineEquipmentAttachment() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        harness.enterBattlefieldAndReturn(player1, new CloudExSOLDIER());
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can enter with no Equipment available")
    void entersWithoutEquipment() {
        Permanent cloud = harness.enterBattlefieldAndReturn(player1, new CloudExSOLDIER());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        if (choice != null) {
            harness.handlePermanentChosen(player1, player1.getId());
            harness.passBothPriorities();
        }

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(cloud);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can move your Equipment from another creature but cannot target opposing Equipment")
    void movesOnlyControlledEquipment() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        equipment.setAttachedTo(bear.getId());
        Permanent opposingEquipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        Permanent cloud = harness.enterBattlefieldAndReturn(player1, new CloudExSOLDIER());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(equipment.getId())
                .doesNotContain(opposingEquipment.getId(), bear.getId(), cloud.getId());
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(cloud.getId());
        assertThat(opposingEquipment.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Does not attach Equipment if Cloud leaves before the entry trigger resolves")
    void doesNotAttachAfterCloudLeaves() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent cloud = harness.enterBattlefieldAndReturn(player1, new CloudExSOLDIER());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, equipment.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, cloud));
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(cloud);
    }

    @Test
    @DisplayName("Counts equipped creatures once regardless of how many Equipment they carry")
    void countsCreaturesRatherThanEquipment() {
        Permanent cloud = addCreatureReady(player1, new CloudExSOLDIER());
        attachEquipment(cloud);
        attachEquipment(cloud);
        Permanent unequippedBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBear = addCreatureReady(player2, new GrizzlyBears());
        Permanent opposingEquipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        opposingEquipment.setAttachedTo(opposingBear.getId());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(player1, List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(cloud),
                gd.playerBattlefields.get(player1.getId()).indexOf(unequippedBear)));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Checks Equipment and power when the attack trigger resolves")
    void checksAttackConditionsAtResolution() {
        Permanent cloud = addCreatureReady(player1, new CloudExSOLDIER());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        equipment.setAttachedTo(cloud.getId());
        cloud.setPowerModifier(2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(cloud)));
        equipment.setAttachedTo(null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Can create Treasures without equipped attackers after gaining power in response")
    void createsTreasuresWithoutEquippedAttackers() {
        Permanent cloud = addCreatureReady(player1, new CloudExSOLDIER());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(cloud)));
        cloud.setPowerModifier(3);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    @DisplayName("Uses Cloud's last known equipped power if it leaves before the attack trigger resolves")
    void usesLastKnownPowerAfterCloudLeaves() {
        Permanent cloud = addCreatureReady(player1, new CloudExSOLDIER());
        attachEquipment(cloud);
        attachEquipment(cloud);
        attachEquipment(cloud);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(cloud)));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, cloud));
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(cloud.getCard());
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    private void attachEquipment(Permanent creature) {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        equipment.setAttachedTo(creature.getId());
    }
}
