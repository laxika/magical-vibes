package com.github.laxika.magicalvibes.cards.s;

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

class SpinEngineTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Spin Engine has one activated ability")
    void hasOneActivatedAbility() {
        SpinEngine card = new SpinEngine();
        assertThat(card.getActivatedAbilities()).hasSize(1);
    }

    // ===== Ability activation =====

    @Test
    @DisplayName("Activating ability puts it on the stack targeting an opponent's creature")
    void activatingAbilityPutsOnStack() {
        Permanent engine = addReadySpinEngine(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Spin Engine");
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Ability does not require tapping")
    void abilityDoesNotRequireTapping() {
        Permanent engine = addReadySpinEngine(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(engine.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        addReadySpinEngine(player1);
        Permanent target = addReadyCreature(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    // ===== Ability resolution =====

    @Test
    @DisplayName("Resolving ability adds source to target's cantBlockIds")
    void resolvingAbilityAddsCantBlockRestriction() {
        Permanent engine = addReadySpinEngine(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCantBlockIds()).contains(engine.getId());
    }

    @Test
    @DisplayName("Ability fizzles if target is removed before resolution")
    void abilityFizzlesIfTargetRemoved() {
        Permanent engine = addReadySpinEngine(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        // Should resolve without error (fizzle)
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    // ===== Blocking restrictions =====

    @Test
    @DisplayName("Targeted creature cannot block Spin Engine after ability resolves")
    void targetedCreatureCannotBlockSpinEngine() {
        Permanent engine = addReadySpinEngine(player1);
        Permanent blocker = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.RED, 1);

        // Activate and resolve the ability
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        // Set up combat: Spin Engine attacks
        engine.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        // Attempting to block Spin Engine with the targeted creature should fail
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't block");
    }

    @Test
    @DisplayName("Targeted creature can still block other creatures")
    void targetedCreatureCanBlockOtherCreatures() {
        Permanent engine = addReadySpinEngine(player1);
        Permanent otherAttacker = addReadyCreature(player1);
        Permanent blocker = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.RED, 1);

        // Activate and resolve the ability targeting the blocker
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        // Set up combat: only the other creature attacks (not Spin Engine)
        otherAttacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        // Blocker can block other creatures — declareBlockers succeeds without throwing
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
    }

    @Test
    @DisplayName("Non-targeted creature can still block Spin Engine")
    void nonTargetedCreatureCanBlockSpinEngine() {
        Permanent engine = addReadySpinEngine(player1);
        Permanent targetedBlocker = addReadyCreature(player2);
        Permanent otherBlocker = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.RED, 1);

        // Activate and resolve the ability targeting only the first blocker
        harness.activateAbility(player1, 0, null, targetedBlocker.getId());
        harness.passBothPriorities();

        // Set up combat: Spin Engine attacks
        engine.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        // otherBlocker (index 1) blocks Spin Engine (index 0) — succeeds
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));
    }

    // ===== Multiple activations =====

    @Test
    @DisplayName("Can activate ability multiple times on different creatures")
    void canActivateMultipleTimes() {
        Permanent engine = addReadySpinEngine(player1);
        Permanent blocker1 = addReadyCreature(player2);
        Permanent blocker2 = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.RED, 2);

        // Activate on first blocker and resolve
        harness.activateAbility(player1, 0, null, blocker1.getId());
        harness.passBothPriorities();

        // Activate on second blocker and resolve
        harness.activateAbility(player1, 0, null, blocker2.getId());
        harness.passBothPriorities();

        assertThat(blocker1.getCantBlockIds()).contains(engine.getId());
        assertThat(blocker2.getCantBlockIds()).contains(engine.getId());
    }

    // ===== End of turn reset =====

    @Test
    @DisplayName("Blocking restriction resets at end of turn")
    void restrictionResetsAtEndOfTurn() {
        Permanent engine = addReadySpinEngine(player1);
        Permanent blocker = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.RED, 1);

        // Activate and resolve the ability
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getCantBlockIds()).contains(engine.getId());

        // Simulate end-of-turn reset
        blocker.resetModifiers();

        assertThat(blocker.getCantBlockIds()).isEmpty();
    }

    // ===== Helper methods =====

    private Permanent addReadySpinEngine(Player player) {
        SpinEngine card = new SpinEngine();
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
