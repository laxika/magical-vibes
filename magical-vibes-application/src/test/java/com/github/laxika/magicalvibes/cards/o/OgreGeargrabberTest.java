package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.cards.v.VolitionReins;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;

import com.github.laxika.magicalvibes.cards.c.CarapaceForger;
import com.github.laxika.magicalvibes.cards.a.AccordersShield;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OgreGeargrabber.class, CarapaceForger.class, AccordersShield.class, VolitionReins.class})
class OgreGeargrabberTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with Ogre Geargrabber when opponent has Equipment prompts for target selection")
    void attackTriggerPromptsForTargetSelection() {
        addCreatureReady(player1, new OgreGeargrabber());
        harness.addToBattlefield(player2, new AccordersShield());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
    }

    @Test
    @DisplayName("Only opponent's Equipment is offered as valid targets")
    void onlyOpponentEquipmentIsValidTarget() {
        Permanent ogre = addCreatureReady(player1, new OgreGeargrabber());
        Permanent ownEquipment = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        Permanent opponentEquipment = harness.addToBattlefieldAndReturn(player2, new AccordersShield());
        Permanent opponentCreature = addCreatureReady(player2, new CarapaceForger());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        // The valid permanent IDs should only include the opponent's Equipment
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(opponentEquipment.getId())
                .doesNotContain(ownEquipment.getId())
                .doesNotContain(opponentCreature.getId())
                .doesNotContain(ogre.getId());
    }

    @Test
    @DisplayName("Attack trigger is skipped when opponent has no Equipment")
    void noTriggerWhenNoOpponentEquipment() {
        addCreatureReady(player1, new OgreGeargrabber());
        addCreatureReady(player2, new CarapaceForger());

        declareAttackers(player1, List.of(0));

        // No permanent choice should be requested
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Resolving attack trigger steals Equipment and attaches it to Ogre Geargrabber")
    void resolvingStealsAndAttachesEquipment() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent ogre = addCreatureReady(player1, new OgreGeargrabber());
        Permanent opponentEquipment = harness.addToBattlefieldAndReturn(player2, new AccordersShield());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, opponentEquipment.getId());
        harness.passBothPriorities();

        // Equipment should now be on player1's battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(opponentEquipment.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(opponentEquipment.getId()));

        // Equipment should be attached to Ogre Geargrabber
        assertThat(opponentEquipment.getAttachedTo()).isEqualTo(ogre.getId());
    }

    @Test
    @DisplayName("Choosing target puts triggered ability on the stack with correct target")
    void choosingTargetPutsAbilityOnStack() {
        Permanent ogre = addCreatureReady(player1, new OgreGeargrabber());
        Permanent opponentEquipment = harness.addToBattlefieldAndReturn(player2, new AccordersShield());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, opponentEquipment.getId());

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Ogre Geargrabber")
                        && se.getTargetId().equals(opponentEquipment.getId())
                        && se.getSourcePermanentId().equals(ogre.getId()));
    }

    @Test
    @DisplayName("Equipment returns to opponent's control at end of turn, unattached")
    void equipmentReturnsAtEndOfTurn() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent ogre = addCreatureReady(player1, new OgreGeargrabber());
        Permanent opponentEquipment = harness.addToBattlefieldAndReturn(player2, new AccordersShield());

        // Steal the equipment during attack
        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, opponentEquipment.getId());
        harness.passBothPriorities();

        // Verify equipment is stolen and attached
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(opponentEquipment.getId()));
        assertThat(opponentEquipment.getAttachedTo()).isEqualTo(ogre.getId());

        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentEquipment);
        assertThat(opponentEquipment.getAttachedTo()).isEqualTo(ogre.getId());
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        harness.passBothPriorities();

        // Equipment should return to player2's control
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(opponentEquipment.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(opponentEquipment.getId()));

        // Equipment should be unattached
        assertThat(opponentEquipment.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Stealing Equipment that was attached to opponent's creature reattaches to Ogre")
    void stealingAttachedEquipmentReattachesToOgre() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent ogre = addCreatureReady(player1, new OgreGeargrabber());
        Permanent opponentCreature = addCreatureReady(player2, new CarapaceForger());
        Permanent opponentEquipment = harness.addToBattlefieldAndReturn(player2, new AccordersShield());
        opponentEquipment.setAttachedTo(opponentCreature.getId());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, opponentEquipment.getId());
        harness.passBothPriorities();

        // Equipment should be attached to Ogre, not the opponent's creature
        assertThat(opponentEquipment.getAttachedTo()).isEqualTo(ogre.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(opponentEquipment.getId()));
    }

    @Test
    @DisplayName("Trigger fizzles if target Equipment is removed before resolution")
    void triggerFizzlesIfEquipmentRemoved() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addCreatureReady(player1, new OgreGeargrabber());
        Permanent opponentEquipment = harness.addToBattlefieldAndReturn(player2, new AccordersShield());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, opponentEquipment.getId());

        // Remove the equipment before resolution
        gd.playerBattlefields.get(player2.getId()).remove(opponentEquipment);

        harness.passBothPriorities();

        // Equipment should not be on any battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(opponentEquipment.getId()));
    }

    @Test
    @DisplayName("Equipment remains stolen and attached during the end step")
    void equipmentRemainsStolenDuringEndStep() {
        Permanent ogre = addCreatureReady(player1, new OgreGeargrabber());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new AccordersShield());
        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(equipment);
        assertThat(equipment.getAttachedTo()).isEqualTo(ogre.getId());
    }

    @Test
    @DisplayName("Still gains control when Ogre leaves before its attack trigger resolves")
    void gainsControlWhenOgreLeavesBeforeResolution() {
        Permanent ogre = addCreatureReady(player1, new OgreGeargrabber());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new AccordersShield());
        Permanent opponentCreature = addCreatureReady(player2, new CarapaceForger());
        equipment.setAttachedTo(opponentCreature.getId());
        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, equipment.getId());
        gd.playerBattlefields.get(player1.getId()).remove(ogre);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(equipment);
        assertThat(equipment.getAttachedTo()).isEqualTo(opponentCreature.getId());
    }

    @Test
    @DisplayName("Losing control before cleanup queues an unattach trigger that can be responded to")
    void losingControlBeforeCleanupQueuesUnattachTrigger() {
        Permanent ogre = addCreatureReady(player1, new OgreGeargrabber());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new AccordersShield());
        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new VolitionReins()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player2, 0, equipment.getId());
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(equipment);
        assertThat(equipment.getAttachedTo()).isEqualTo(ogre.getId());
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());

        harness.passBothPriorities();
        assertThat(equipment.getAttachedTo()).isNull();
    }
    @Test
    @DisplayName("Attack trigger fizzles if Equipment is no longer controlled by an opponent")
    void targetBecomesIllegalWhenItsControllerChanges() {
        Permanent ogre = addCreatureReady(player1, new OgreGeargrabber());
        Permanent opponentCreature = addCreatureReady(player2, new CarapaceForger());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new AccordersShield());
        equipment.setAttachedTo(opponentCreature.getId());
        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, equipment.getId());

        // Put a real control-changing Aura onto the battlefield before the attack ability resolves.
        Permanent reins = harness.addToBattlefieldAndReturn(player1, new VolitionReins());
        reins.setAttachedTo(equipment.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(equipment);
        assertThat(equipment.getAttachedTo()).isEqualTo(opponentCreature.getId());
        assertThat(equipment.getAttachedTo()).isNotEqualTo(ogre.getId());
        assertThat(gd.stack).isEmpty();
    }

}
