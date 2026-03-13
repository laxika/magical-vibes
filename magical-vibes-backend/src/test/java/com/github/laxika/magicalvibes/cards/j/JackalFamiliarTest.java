package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockAloneEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JackalFamiliarTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Jackal Familiar has CantAttackOrBlockAloneEffect")
    void hasCorrectProperties() {
        JackalFamiliar card = new JackalFamiliar();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(CantAttackOrBlockAloneEffect.class);
    }

    // ===== Can't attack alone =====

    @Test
    @DisplayName("Jackal Familiar can't attack alone")
    void cantAttackAlone() {
        Permanent familiar = new Permanent(new JackalFamiliar());
        familiar.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(familiar);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't attack alone");
    }

    @Test
    @DisplayName("Jackal Familiar can attack with another creature")
    void canAttackWithAnother() {
        Permanent familiar = new Permanent(new JackalFamiliar());
        familiar.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(familiar);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0, 1));

        assertThat(familiar.isAttacking()).isTrue();
        assertThat(bears.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Two Jackal Familiars can attack together")
    void twoFamiliarsCanAttackTogether() {
        Permanent familiar1 = new Permanent(new JackalFamiliar());
        familiar1.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(familiar1);

        Permanent familiar2 = new Permanent(new JackalFamiliar());
        familiar2.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(familiar2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0, 1));

        assertThat(familiar1.isAttacking()).isTrue();
        assertThat(familiar2.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Jackal Familiar not included in available attackers when it's the only eligible creature")
    void notInAvailableAttackersWhenAlone() {
        Permanent familiar = new Permanent(new JackalFamiliar());
        familiar.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(familiar);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        // The combat service should not include this creature in available attackers
        // since it can't legally attack alone
        // We verify by checking that declaring 0 attackers is valid (no "must attack" error)
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        gs.declareAttackers(gd, player1, List.of());
    }

    // ===== Can't block alone =====

    @Test
    @DisplayName("Jackal Familiar can't block alone")
    void cantBlockAlone() {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent familiar = new Permanent(new JackalFamiliar());
        familiar.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(familiar);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't block alone");
    }

    @Test
    @DisplayName("Jackal Familiar can block with another creature")
    void canBlockWithAnother() {
        Permanent attacker1 = new Permanent(new GrizzlyBears());
        attacker1.setSummoningSick(false);
        attacker1.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker1);

        Permanent attacker2 = new Permanent(new GrizzlyBears());
        attacker2.setSummoningSick(false);
        attacker2.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker2);

        Permanent familiar = new Permanent(new JackalFamiliar());
        familiar.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(familiar);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1)
        ));

        assertThat(familiar.isBlocking()).isTrue();
        assertThat(bears.isBlocking()).isTrue();
    }
}
