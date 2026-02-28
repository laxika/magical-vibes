package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TangleAnglerTest extends BaseCardTest {

    // ===== Ability activation =====

    @Test
    @DisplayName("Tangle Angler has one activated ability")
    void hasOneActivatedAbility() {
        TangleAngler card = new TangleAngler();
        assertThat(card.getActivatedAbilities()).hasSize(1);
    }

    @Test
    @DisplayName("Activating ability puts it on the stack targeting a creature")
    void activatingAbilityPutsOnStack() {
        Permanent angler = addReadyAngler(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Tangle Angler");
        assertThat(entry.getTargetPermanentId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Ability does not require tapping")
    void abilityDoesNotRequireTapping() {
        Permanent angler = addReadyAngler(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(angler.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        addReadyAngler(player1);
        Permanent target = addReadyCreature(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    // ===== Ability resolution =====

    @Test
    @DisplayName("Resolving ability adds source to target's mustBlockIds")
    void resolvingAbilityAddsMustBlockRestriction() {
        Permanent angler = addReadyAngler(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMustBlockIds()).contains(angler.getId());
    }

    @Test
    @DisplayName("Ability fizzles if target is removed before resolution")
    void abilityFizzlesIfTargetRemoved() {
        addReadyAngler(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        // Should resolve without error (fizzle)
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    // ===== Blocking requirements =====

    @Test
    @DisplayName("Targeted creature must block Tangle Angler when it attacks")
    void targetedCreatureMustBlockTangleAngler() {
        Permanent angler = addReadyAngler(player1);
        Permanent blocker = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        // Activate and resolve the ability
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        // Set up combat: Tangle Angler attacks
        angler.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        // Attempting to declare no blockers should fail
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
    }

    @Test
    @DisplayName("Targeted creature satisfies requirement by blocking Tangle Angler")
    void targetedCreatureCanSatisfyRequirement() {
        Permanent angler = addReadyAngler(player1);
        Permanent blocker = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        // Activate and resolve the ability
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        // Set up combat: Tangle Angler attacks
        angler.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        // Blocker at index 0 blocks attacker at index 0 (Tangle Angler)
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    @Test
    @DisplayName("No requirement if Tangle Angler is not attacking")
    void noRequirementIfAnglerNotAttacking() {
        Permanent angler = addReadyAngler(player1);
        Permanent otherAttacker = addReadyCreature(player1);
        Permanent blocker = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        // Activate and resolve the ability
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        // Set up combat: only the other creature attacks (not Tangle Angler)
        otherAttacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        // Declaring no blockers should succeed — no must-block requirement applies
        gs.declareBlockers(gd, player2, List.of());
    }

    @Test
    @DisplayName("Can activate ability on multiple creatures")
    void canActivateOnMultipleCreatures() {
        Permanent angler = addReadyAngler(player1);
        Permanent blocker1 = addReadyCreature(player2);
        Permanent blocker2 = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        // Activate on first blocker and resolve
        harness.activateAbility(player1, 0, null, blocker1.getId());
        harness.passBothPriorities();

        // Activate on second blocker and resolve
        harness.activateAbility(player1, 0, null, blocker2.getId());
        harness.passBothPriorities();

        assertThat(blocker1.getMustBlockIds()).contains(angler.getId());
        assertThat(blocker2.getMustBlockIds()).contains(angler.getId());
    }

    @Test
    @DisplayName("Targeted creature does not need to block if it can't legally block (e.g. tapped)")
    void noRequirementIfBlockerCannotBlock() {
        Permanent angler = addReadyAngler(player1);
        Permanent blocker = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        // Activate and resolve the ability
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        // Tap the blocker so it can't block
        blocker.tap();

        // Set up combat: Tangle Angler attacks
        angler.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        // Declaring no blockers should succeed — tapped creature can't block
        gs.declareBlockers(gd, player2, List.of());
    }

    // ===== End of turn reset =====

    @Test
    @DisplayName("Must-block restriction resets at end of turn")
    void restrictionResetsAtEndOfTurn() {
        Permanent angler = addReadyAngler(player1);
        Permanent blocker = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        // Activate and resolve the ability
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getMustBlockIds()).contains(angler.getId());

        // Simulate end-of-turn reset (resetModifiers clears mustBlockIds)
        blocker.resetModifiers();

        assertThat(blocker.getMustBlockIds()).isEmpty();
    }

    // ===== Can target own creatures =====

    @Test
    @DisplayName("Can activate ability targeting own creature")
    void canTargetOwnCreature() {
        Permanent angler = addReadyAngler(player1);
        Permanent ownCreature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getMustBlockIds()).contains(angler.getId());
    }

    // ===== Helper methods =====

    private Permanent addReadyAngler(Player player) {
        TangleAngler card = new TangleAngler();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        GrizzlyBears card = new GrizzlyBears();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
