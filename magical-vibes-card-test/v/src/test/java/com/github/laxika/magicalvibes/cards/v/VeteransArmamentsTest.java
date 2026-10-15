package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VeteransArmaments.class, GrizzlyBears.class, VeteranArmorsmith.class})
class VeteransArmamentsTest extends BaseCardTest {

    // ===== Granted trigger: "Whenever this creature attacks or blocks, it gets +1/+1
    //       until end of turn for each attacking creature." =====

    @Test
    @DisplayName("Equipped creature attacking alone gets +1/+1 (one attacking creature)")
    void attackingAloneGetsPlusOne() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent armaments = addCreatureReady(player1, new VeteransArmaments());
        armaments.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(1);
        assertThat(creature.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost scales with the number of attacking creatures")
    void boostScalesWithAttackers() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent armaments = addCreatureReady(player1, new VeteransArmaments());
        armaments.setAttachedTo(creature.getId());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        // Indices 0 (equipped), 2 and 3 are creatures (index 1 is the Equipment).
        declareAttackers(player1, List.of(0, 2, 3));
        harness.passBothPriorities();

        // Three attacking creatures → +3/+3.
        assertThat(creature.getPowerModifier()).isEqualTo(3);
        assertThat(creature.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent armaments = addCreatureReady(player1, new VeteransArmaments());
        armaments.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(0);
        assertThat(creature.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Equipped creature that blocks is boosted per attacking creature")
    void blockingCreatureGetsBoost() {
        // Player 1 attacks with two creatures.
        Permanent attacker1 = addCreatureReady(player1, new GrizzlyBears());
        Permanent attacker2 = addCreatureReady(player1, new GrizzlyBears());
        attacker1.setAttacking(true);
        attacker2.setAttacking(true);

        // Player 2's equipped creature blocks.
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent armaments = addCreatureReady(player2, new VeteransArmaments());
        armaments.setAttachedTo(blocker.getId());

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // Two attacking creatures → +2/+2 on the blocker.
        assertThat(blocker.getPowerModifier()).isEqualTo(2);
        assertThat(blocker.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("No trigger when the Equipment is not attached to the attacker")
    void noTriggerWhenUnattached() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new VeteransArmaments()); // present but unattached

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack)
                .noneMatch(se -> se.getCard().getName().equals("Veteran's Armaments"));
        assertThat(creature.getPowerModifier()).isEqualTo(0);
    }

    // ===== Trigger: "Whenever a Soldier creature enters, you may attach this Equipment to it." =====

    @Test
    @DisplayName("Accepting the may attaches the Equipment to the Soldier that entered")
    void attachesToEnteringSoldierOnAccept() {
        Permanent armaments = addCreatureReady(player1, new VeteransArmaments());

        harness.castFromHand(player1, new VeteranArmorsmith(), "{W}{W}");

        harness.passBothPriorities(); // resolve creature spell → trigger, may-ability on stack
        harness.passBothPriorities(); // resolve may-ability → may prompt

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        Permanent soldier = findPermanent(player1, "Veteran Armorsmith");
        assertThat(armaments.getAttachedTo()).isEqualTo(soldier.getId());
    }

    @Test
    @DisplayName("Does not trigger for a non-Soldier creature entering")
    void doesNotTriggerForNonSoldier() {
        addCreatureReady(player1, new VeteransArmaments());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    // ===== Equip {2} =====

    @Test
    @DisplayName("Resolving equip attaches the Equipment to the target creature")
    void resolvingEquipAttaches() {
        Permanent armaments = addCreatureReady(player1, new VeteransArmaments());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(armaments.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Declining the attachment leaves the Equipment on its previous creature")
    void decliningAttachmentKeepsPreviousCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent armaments = harness.addToBattlefieldAndReturn(player1, new VeteransArmaments());
        armaments.setAttachedTo(creature.getId());

        harness.castFromHand(player1, new VeteranArmorsmith(), "{W}{W}");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(armaments.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("The Equipment controller may attach it to an opponent's entering Soldier")
    void attachesToOpponentsSoldier() {
        Permanent armaments = harness.addToBattlefieldAndReturn(player1, new VeteransArmaments());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new VeteranArmorsmith(), "{W}{W}");

        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        Permanent soldier = findPermanent(player2, "Veteran Armorsmith");
        assertThat(armaments.getAttachedTo()).isEqualTo(soldier.getId());
    }

    @Test
    @DisplayName("A pending combat boost remains with the creature that triggered it after reattachment")
    void pendingBoostRemainsWithOriginalCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent armaments = harness.addToBattlefieldAndReturn(player1, new VeteransArmaments());
        armaments.setAttachedTo(attacker.getId());

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);
        Permanent soldier = harness.enterBattlefieldAndReturn(player1, new VeteranArmorsmith());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(armaments.getAttachedTo()).isEqualTo(soldier.getId());
        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        assertThat(attacker.getToughnessModifier()).isEqualTo(1);
        assertThat(soldier.getPowerModifier()).isZero();
        assertThat(soldier.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Unattaching the Equipment does not stop a pending combat boost")
    void pendingBoostSurvivesUnattachment() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent armaments = harness.addToBattlefieldAndReturn(player1, new VeteransArmaments());
        armaments.setAttachedTo(attacker.getId());

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);
        armaments.setAttachedTo(null);
        resolveAllTriggers();

        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        assertThat(attacker.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("The equipped creature's controller controls its granted combat trigger")
    void creatureControllerControlsGrantedTrigger() {
        Permanent armaments = harness.addToBattlefieldAndReturn(player1, new VeteransArmaments());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        armaments.setAttachedTo(attacker.getId());

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).singleElement()
                .satisfies(entry -> assertThat(entry.getControllerId()).isEqualTo(player2.getId()));
        resolveAllTriggers();
        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        assertThat(attacker.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("The boost counts attackers when it resolves rather than when it triggers")
    void countsAttackersAtResolution() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent armaments = harness.addToBattlefieldAndReturn(player1, new VeteransArmaments());
        armaments.setAttachedTo(attacker.getId());
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 2));
        assertThat(gd.stack).hasSize(1);
        otherAttacker.setAttacking(false);
        resolveAllTriggers();

        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        assertThat(attacker.getToughnessModifier()).isEqualTo(1);
    }

}
