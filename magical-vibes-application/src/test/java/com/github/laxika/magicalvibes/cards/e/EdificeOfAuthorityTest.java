package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EdificeOfAuthorityTest extends BaseCardTest {

    // ===== First ability: can't attack this turn + brick counter =====

    @Test
    @DisplayName("First ability adds a brick counter and stops the target from attacking this turn")
    void firstAbilityLocksAttackAndAddsBrickCounter() {
        Permanent edifice = addReadyEdifice(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(edifice.getCounterCount(CounterType.BRICK)).isEqualTo(1);
        assertThatThrownBy(() -> declareBearsAttack(bears))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("First ability's attack lock wears off at end of turn")
    void firstAbilityLockWearsOffAtEndOfTurn() {
        addReadyEdifice(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        gd.expireEndOfTurnFloatingEffects();

        assertThatCode(() -> declareBearsAttack(bears)).doesNotThrowAnyException();
    }

    // ===== Second ability: activation gate on brick counters =====

    @Test
    @DisplayName("Second ability can't be activated with fewer than three brick counters")
    void secondAbilityRequiresThreeBrickCounters() {
        Permanent edifice = addReadyEdifice(player1);
        edifice.setCounterCount(CounterType.BRICK, 2);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("brick counters");
    }

    @Test
    @DisplayName("Second ability activates with three brick counters")
    void secondAbilityActivatesWithThreeBrickCounters() {
        Permanent edifice = addReadyEdifice(player1);
        edifice.setCounterCount(CounterType.BRICK, 3);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, bears.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bears.getId());
    }

    // ===== Second ability: detain (can't attack / block / activate) =====

    @Test
    @DisplayName("Detained creature can't attack")
    void detainedCreatureCannotAttack() {
        Permanent bears = detainOwnCreature(new GrizzlyBears());

        assertThatThrownBy(() -> declareBearsAttack(bears))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Detained creature can't block")
    void detainedCreatureCannotBlock() {
        Permanent edifice = addReadyEdifice(player1);
        edifice.setCounterCount(CounterType.BRICK, 3);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, blocker.getId());
        harness.passBothPriorities();

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't block");
    }

    @Test
    @DisplayName("Detained creature can't activate its abilities (mana abilities included)")
    void detainedCreatureCannotActivateAbilities() {
        Permanent edifice = addReadyEdifice(player1);
        edifice.setCounterCount(CounterType.BRICK, 3);
        Permanent elves = addCreatureReady(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, elves.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.tapPermanent(player1, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Detain wears off at the ability controller's next turn")
    void detainWearsOffAtControllersNextTurn() {
        Permanent bears = detainOwnCreature(new GrizzlyBears());

        gd.expireFloatingEffectsAtTurnStart(player1.getId());

        assertThatCode(() -> declareBearsAttack(bears)).doesNotThrowAnyException();
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addReadyEdifice(player1);
        Permanent land = new Permanent(new Forest());
        gd.playerBattlefields.get(player2.getId()).add(land);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Helpers =====

    private Permanent addReadyEdifice(Player player) {
        Permanent perm = new Permanent(new EdificeOfAuthority());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    /** Detains a creature player1 controls via the second ability (three brick counters). */
    private Permanent detainOwnCreature(com.github.laxika.magicalvibes.model.Card card) {
        Permanent edifice = addReadyEdifice(player1);
        edifice.setCounterCount(CounterType.BRICK, 3);
        Permanent creature = addCreatureReady(player1, card);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();
        return creature;
    }

    /** Attempts to declare the given player1 creature (battlefield index 1) as an attacker. */
    private void declareBearsAttack(Permanent creature) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(creature);
        gs.declareAttackers(gd, player1, List.of(index));
    }
}
