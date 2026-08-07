package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NivixBarrierTest extends BaseCardTest {

    private Permanent addAttacker(Permanent attacker) {
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(attacker);
        return attacker;
    }

    @Test
    @DisplayName("Can be cast during the opponent's declare attackers step thanks to Flash")
    void canCastAtInstantSpeed() {
        Permanent attacker = addAttacker(new Permanent(new HillGiant()));

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new NivixBarrier()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.passPriority(player1);

        gs.playCard(gd, player2, 0, 0, attacker.getId(), null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(attacker.getId());
    }

    @Test
    @DisplayName("ETB gives the target attacking creature -4/-0")
    void etbWeakensAttacker() {
        Permanent attacker = addAttacker(new Permanent(new HillGiant()));

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new NivixBarrier()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.passPriority(player1);

        gs.playCard(gd, player2, 0, 0, attacker.getId(), null);
        harness.passBothPriorities(); // creature resolves, ETB trigger goes on the stack
        harness.passBothPriorities(); // ETB resolves

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Nivix Barrier");
        assertThat(attacker.getPowerModifier()).isEqualTo(-4);
        assertThat(attacker.getToughnessModifier()).isEqualTo(0);
        // A 3/3 given -4/-0 is a -1/3 creature; power is not floored at 0 (CR 107.1b).
        assertThat(attacker.getEffectivePower()).isEqualTo(-1);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Power reduction wears off at end of turn")
    void debuffWearsOff() {
        Permanent attacker = addAttacker(new Permanent(new HillGiant()));

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new NivixBarrier()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.passPriority(player1);

        gs.playCard(gd, player2, 0, 0, attacker.getId(), null);
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
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new NivixBarrier()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.passPriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0, bearsId, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an attacking creature");

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB fizzles if the target attacker leaves before resolution")
    void etbFizzlesIfTargetRemoved() {
        Permanent attacker = addAttacker(new Permanent(new HillGiant()));

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new NivixBarrier()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.passPriority(player1);

        gs.playCard(gd, player2, 0, 0, attacker.getId(), null);
        harness.passBothPriorities(); // creature resolves, ETB trigger on the stack

        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }
}
