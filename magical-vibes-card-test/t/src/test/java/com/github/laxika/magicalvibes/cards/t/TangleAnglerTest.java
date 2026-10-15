package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.s.SylvokReplica;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TangleAngler.class, SylvokReplica.class})
class TangleAnglerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability puts it on the stack targeting a creature")
    void activatingAbilityPutsOnStack() {
        Permanent angler = addReadyAngler(player1);
        Permanent target = addCreatureReady(player2, new SylvokReplica());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getSourcePermanentId()).isEqualTo(angler.getId());
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Ability does not require tapping")
    void abilityDoesNotRequireTapping() {
        Permanent angler = addReadyAngler(player1);
        Permanent target = addCreatureReady(player2, new SylvokReplica());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(angler.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        addReadyAngler(player1);
        Permanent target = addCreatureReady(player2, new SylvokReplica());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Resolving ability adds source to target's mustBlockIds")
    void resolvingAbilityAddsMustBlockRestriction() {
        Permanent angler = addReadyAngler(player1);
        Permanent target = addCreatureReady(player2, new SylvokReplica());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMustBlockIds()).contains(angler.getId());
    }

    @Test
    @DisplayName("Ability fizzles if target is removed before resolution")
    void abilityFizzlesIfTargetRemoved() {
        addReadyAngler(player1);
        Permanent target = addCreatureReady(player2, new SylvokReplica());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        // Should resolve without error (fizzle)
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Targeted creature must block Tangle Angler when it attacks")
    void targetedCreatureMustBlockTangleAngler() {
        Permanent angler = addReadyAngler(player1);
        Permanent blocker = addCreatureReady(player2, new SylvokReplica());
        harness.addMana(player1, ManaColor.GREEN, 1);

        // Activate and resolve the ability
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        // Set up combat: Tangle Angler attacks
        angler.setAttacking(true);
        prepareDeclareBlockers();

        // Attempting to declare no blockers should fail
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
    }

    @Test
    @DisplayName("Targeted creature satisfies requirement by blocking Tangle Angler")
    void targetedCreatureCanSatisfyRequirement() {
        Permanent angler = addReadyAngler(player1);
        Permanent blocker = addCreatureReady(player2, new SylvokReplica());
        harness.addMana(player1, ManaColor.GREEN, 1);

        // Activate and resolve the ability
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        // Set up combat: Tangle Angler attacks
        angler.setAttacking(true);
        prepareDeclareBlockers();

        // Blocker at index 0 blocks attacker at index 0 (Tangle Angler)
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    @Test
    @DisplayName("No requirement if Tangle Angler is not attacking")
    void noRequirementIfAnglerNotAttacking() {
        addReadyAngler(player1);
        Permanent otherAttacker = addCreatureReady(player1, new SylvokReplica());
        Permanent blocker = addCreatureReady(player2, new SylvokReplica());
        harness.addMana(player1, ManaColor.GREEN, 1);

        // Activate and resolve the ability
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        // Set up combat: only the other creature attacks (not Tangle Angler)
        otherAttacker.setAttacking(true);
        prepareDeclareBlockers();

        // Declaring no blockers should succeed — no must-block requirement applies
        gs.declareBlockers(gd, player2, List.of());
    }

    @Test
    @DisplayName("Can activate ability on multiple creatures")
    void canActivateOnMultipleCreatures() {
        Permanent angler = addReadyAngler(player1);
        Permanent blocker1 = addCreatureReady(player2, new SylvokReplica());
        Permanent blocker2 = addCreatureReady(player2, new SylvokReplica());
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
        Permanent blocker = addCreatureReady(player2, new SylvokReplica());
        harness.addMana(player1, ManaColor.GREEN, 1);

        // Activate and resolve the ability
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        // Tap the blocker so it can't block
        blocker.tap();

        // Set up combat: Tangle Angler attacks
        angler.setAttacking(true);
        prepareDeclareBlockers();

        // Declaring no blockers should succeed — tapped creature can't block
        gs.declareBlockers(gd, player2, List.of());
    }

    @Test
    @DisplayName("Must-block restriction resets at end of turn")
    void restrictionResetsAtEndOfTurn() {
        Permanent angler = addReadyAngler(player1);
        Permanent blocker = addCreatureReady(player2, new SylvokReplica());
        harness.addMana(player1, ManaColor.GREEN, 1);

        // Activate and resolve the ability
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getMustBlockIds()).contains(angler.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getMustBlockIds()).isEmpty();
    }

    @Test
    @DisplayName("Can activate ability targeting own creature")
    void canTargetOwnCreature() {
        Permanent angler = addReadyAngler(player1);
        Permanent ownCreature = addCreatureReady(player1, new SylvokReplica());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getMustBlockIds()).contains(angler.getId());
    }

    @Test
    @DisplayName("Tapped, summoning-sick Tangle Angler can activate repeatedly")
    void tappedSummoningSickAnglerCanActivateRepeatedly() {
        Permanent angler = harness.addToBattlefieldAndReturn(player1, new TangleAngler());
        angler.tap();
        Permanent target = addCreatureReady(player2, new SylvokReplica());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.activateAbility(player1, 0, null, target.getId());
        resolveAllTriggers();

        assertThat(angler.isTapped()).isTrue();
        assertThat(target.getMustBlockIds()).contains(angler.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Unblocked combat damage gives poison instead of life loss")
    void unblockedCombatDamageGivesPoison() {
        Permanent angler = addReadyAngler(player1);
        angler.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Forced blocker receives infect counters rather than marked damage")
    void forcedBlockerReceivesInfectCounters() {
        Permanent angler = addReadyAngler(player1);
        Permanent blocker = addCreatureReady(player2, new SylvokReplica());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();
        angler.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();

        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Sylvok Replica");
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("A creature required to block two Anglers may block either one")
    void competingRequirementsAllowEitherAngler() {
        Permanent first = addReadyAngler(player1);
        Permanent second = addReadyAngler(player1);
        Permanent blocker = addCreatureReady(player2, new SylvokReplica());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, blocker.getId());
        harness.passBothPriorities();
        first.setAttacking(true);
        second.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
    }

    private Permanent addReadyAngler(Player player) {
        return addCreatureReady(player, new TangleAngler());
    }
}
