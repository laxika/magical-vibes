package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.AwaitingInput;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEquipmentUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OgreGeargrabberTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Ogre Geargrabber has ON_ATTACK gain control of equipment effect")
    void hasOnAttackEffect() {
        OgreGeargrabber card = new OgreGeargrabber();

        assertThat(card.getEffects(EffectSlot.ON_ATTACK)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ATTACK).getFirst())
                .isInstanceOf(GainControlOfTargetEquipmentUntilEndOfTurnEffect.class);
    }

    @Test
    @DisplayName("Ogre Geargrabber has target filter for opponent's Equipment")
    void hasTargetFilter() {
        OgreGeargrabber card = new OgreGeargrabber();

        assertThat(card.getTargetFilter()).isInstanceOf(PermanentPredicateTargetFilter.class);
    }

    // ===== Attack trigger: target selection =====

    @Test
    @DisplayName("Attacking with Ogre Geargrabber when opponent has Equipment prompts for target selection")
    void attackTriggerPromptsForTargetSelection() {
        Permanent ogre = addReadyOgre(player1);
        Permanent opponentEquipment = addEquipment(player2);

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
    }

    @Test
    @DisplayName("Only opponent's Equipment is offered as valid targets")
    void onlyOpponentEquipmentIsValidTarget() {
        Permanent ogre = addReadyOgre(player1);
        Permanent ownEquipment = addEquipment(player1);
        Permanent opponentEquipment = addEquipment(player2);
        Permanent opponentCreature = addReadyCreature(player2);

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        // The valid permanent IDs should only include the opponent's Equipment
        assertThat(gd.interaction.awaitingPermanentChoiceValidIds())
                .contains(opponentEquipment.getId())
                .doesNotContain(ownEquipment.getId())
                .doesNotContain(opponentCreature.getId())
                .doesNotContain(ogre.getId());
    }

    @Test
    @DisplayName("Attack trigger is skipped when opponent has no Equipment")
    void noTriggerWhenNoOpponentEquipment() {
        Permanent ogre = addReadyOgre(player1);
        Permanent opponentCreature = addReadyCreature(player2);

        declareAttackers(player1, List.of(0));

        // No permanent choice should be requested
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.PERMANENT_CHOICE);
    }

    // ===== Resolution: gain control and attach =====

    @Test
    @DisplayName("Resolving attack trigger steals Equipment and attaches it to Ogre Geargrabber")
    void resolvingStealsAndAttachesEquipment() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent ogre = addReadyOgre(player1);
        Permanent opponentEquipment = addEquipment(player2);

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
        Permanent ogre = addReadyOgre(player1);
        Permanent opponentEquipment = addEquipment(player2);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, opponentEquipment.getId());

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Ogre Geargrabber")
                        && se.getTargetPermanentId().equals(opponentEquipment.getId())
                        && se.getSourcePermanentId().equals(ogre.getId()));
    }

    // ===== End of turn: return and unattach =====

    @Test
    @DisplayName("Equipment returns to opponent's control at end of turn, unattached")
    void equipmentReturnsAtEndOfTurn() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent ogre = addReadyOgre(player1);
        Permanent opponentEquipment = addEquipment(player2);

        // Steal the equipment during attack
        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, opponentEquipment.getId());
        harness.passBothPriorities();

        // Verify equipment is stolen and attached
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(opponentEquipment.getId()));
        assertThat(opponentEquipment.getAttachedTo()).isEqualTo(ogre.getId());

        // Advance to end step to trigger end-of-turn resets
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Equipment should return to player2's control
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(opponentEquipment.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(opponentEquipment.getId()));

        // Equipment should be unattached
        assertThat(opponentEquipment.getAttachedTo()).isNull();
    }

    // ===== Equipment attached to opponent creature: steals and reattaches =====

    @Test
    @DisplayName("Stealing Equipment that was attached to opponent's creature reattaches to Ogre")
    void stealingAttachedEquipmentReattachesToOgre() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent ogre = addReadyOgre(player1);
        Permanent opponentCreature = addReadyCreature(player2);
        Permanent opponentEquipment = addEquipment(player2);
        opponentEquipment.setAttachedTo(opponentCreature.getId());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, opponentEquipment.getId());
        harness.passBothPriorities();

        // Equipment should be attached to Ogre, not the opponent's creature
        assertThat(opponentEquipment.getAttachedTo()).isEqualTo(ogre.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(opponentEquipment.getId()));
    }

    // ===== Fizzle: target equipment removed before resolution =====

    @Test
    @DisplayName("Trigger fizzles if target Equipment is removed before resolution")
    void triggerFizzlesIfEquipmentRemoved() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent ogre = addReadyOgre(player1);
        Permanent opponentEquipment = addEquipment(player2);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, opponentEquipment.getId());

        // Remove the equipment before resolution
        gd.playerBattlefields.get(player2.getId()).remove(opponentEquipment);

        harness.passBothPriorities();

        // Equipment should not be on any battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(opponentEquipment.getId()));
    }

    // ===== Helpers =====

    private Permanent addReadyOgre(Player player) {
        Permanent perm = new Permanent(new OgreGeargrabber());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addEquipment(Player player) {
        Permanent perm = new Permanent(new LeoninScimitar());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        gs.declareAttackers(gd, player, attackerIndices);
    }
}
