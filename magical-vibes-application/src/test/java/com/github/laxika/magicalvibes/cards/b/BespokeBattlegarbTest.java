package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({BespokeBattlegarb.class, GrizzlyBears.class, Forest.class})
class BespokeBattlegarbTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+0")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1);
        Permanent equipment = addEquipmentReady(player1);
        equipment.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Celebration attaches the Equipment to a creature you control")
    void celebrationAttachesToCreatureYouControl() {
        Permanent equipment = castEquipment();
        Permanent creature = castCreature();
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToBeginningOfCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(creature.getId()).doesNotContain(opposingCreature.getId());

        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Celebration does not trigger without two nonland permanents entering")
    void celebrationDoesNotTriggerWithoutTwoNonlandPermanents() {
        castEquipment();

        advanceToBeginningOfCombat();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Equip {2} attaches the Equipment to a creature you control")
    void equipAttachesToCreature() {
        Permanent equipment = addEquipmentReady(player1);
        Permanent creature = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Lands do not count toward celebration")
    void landsDoNotCountTowardCelebration() {
        castEquipment();
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        advanceToBeginningOfCombat();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Opposing nonland permanents do not count toward celebration")
    void opposingPermanentsDoNotCountTowardCelebration() {
        castEquipment();
        harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToBeginningOfCombat();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Celebration can choose no target without detaching the Equipment")
    void celebrationCanChooseNoTarget() {
        Permanent equipment = castEquipment();
        Permanent creature = castCreature();
        equipment.setAttachedTo(creature.getId());

        advanceToBeginningOfCombat();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Celebration still triggers when no creature is available")
    void celebrationWithNoCreatureAvailable() {
        Permanent firstEquipment = castEquipment();
        Permanent secondEquipment = harness.enterBattlefieldAndReturn(player1, new BespokeBattlegarb());

        advanceToBeginningOfCombat();

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(firstEquipment.getAttachedTo()).isNull();
        assertThat(secondEquipment.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Celebration moves the Equipment and its boost to the chosen creature")
    void celebrationMovesEquipmentToChosenCreature() {
        Permanent oldCreature = addCreatureReady(player1);
        Permanent equipment = castEquipment();
        Permanent newCreature = castCreature();
        equipment.setAttachedTo(oldCreature.getId());

        advanceToBeginningOfCombat();
        harness.handlePermanentChosen(player1, newCreature.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(newCreature.getId());
        assertThat(gqs.getEffectivePower(gd, oldCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, newCreature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Celebration does not trigger during an opponent's combat")
    void celebrationDoesNotTriggerDuringOpponentsCombat() {
        castEquipment();
        castCreature();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent castEquipment() {
        harness.castFromHand(player1, new BespokeBattlegarb(), "{1}{R}");
        harness.passBothPriorities();
        return findPermanent(player1, "Bespoke Battlegarb");
    }

    private Permanent castCreature() {
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        return findPermanent(player1, "Grizzly Bears");
    }

    private Permanent addCreatureReady(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }

    private Permanent addEquipmentReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new BespokeBattlegarb());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void advanceToBeginningOfCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
