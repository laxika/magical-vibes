package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BoggartBrute;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NivixBarrier.class, BoggartBrute.class})
class NivixBarrierTest extends BaseCardTest {

    private Permanent addAttacker() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new BoggartBrute());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        return attacker;
    }

    @Test
    @DisplayName("Can be cast during the opponent's declare attackers step thanks to Flash")
    void canCastAtInstantSpeed() {
        Permanent attacker = addAttacker();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new NivixBarrier()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.passPriority(player1);

        harness.castCreature(player2, 0, attacker.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(attacker.getId());
    }

    @Test
    @DisplayName("ETB gives the target attacking creature -4/-0")
    void etbWeakensAttacker() {
        Permanent attacker = addAttacker();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new NivixBarrier()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.passPriority(player1);

        harness.castCreature(player2, 0, attacker.getId());
        harness.passBothPriorities(); // creature resolves, ETB trigger goes on the stack
        harness.passBothPriorities(); // ETB resolves

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Nivix Barrier");
        assertThat(attacker.getPowerModifier()).isEqualTo(-4);
        assertThat(attacker.getToughnessModifier()).isEqualTo(0);
        // Power can become negative after the reduction.
        assertThat(attacker.getEffectivePower()).isEqualTo(-1);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Power reduction wears off at end of turn")
    void debuffWearsOff() {
        Permanent attacker = addAttacker();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new NivixBarrier()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.passPriority(player1);

        harness.castCreature(player2, 0, attacker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(-4);

        // Nivix Barrier is a legal blocker, so combat stops for blocker declaration; the pending
        // interaction has to be answered or passing priority below is a no-op and cleanup never runs.
        gs.declareBlockers(gd, player2, List.of());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(0);
        assertThat(attacker.getEffectivePower()).isEqualTo(3);
    }

    @Test
    @DisplayName("A non-attacking creature is not a legal target")
    void cannotTargetNonAttackingCreature() {
        harness.addToBattlefield(player1, new BoggartBrute());
        UUID nonAttackerId = harness.getPermanentId(player1, "Boggart Brute");

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new NivixBarrier()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castCreature(player2, 0, nonAttackerId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an attacking creature");

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB fizzles if the target attacker leaves before resolution")
    void etbFizzlesIfTargetRemoved() {
        Permanent attacker = addAttacker();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new NivixBarrier()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.passPriority(player1);

        harness.castCreature(player2, 0, attacker.getId());
        harness.passBothPriorities(); // creature resolves, ETB trigger on the stack

        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }
    @Test
    @DisplayName("Can resolve without any attacking creature")
    void canCastWithoutAttackers() {
        harness.setHand(player1, List.of(new NivixBarrier()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nivix Barrier");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An attacker removed from combat is no longer a legal trigger target")
    void targetMustStillBeAttackingAtResolution() {
        Permanent attacker = addAttacker();
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player2, List.of(new NivixBarrier()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castCreature(player2, 0, attacker.getId());
        harness.passBothPriorities();
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(attacker.getPowerModifier()).isZero();
        harness.assertOnBattlefield(player2, "Nivix Barrier");
    }

    @Test
    @DisplayName("Defender prevents Nivix Barrier from attacking")
    void cannotAttack() {
        Permanent barrier = harness.addToBattlefieldAndReturn(player1, new NivixBarrier());
        barrier.setSummoningSick(false);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(barrier.isAttacking()).isFalse();
    }
}
