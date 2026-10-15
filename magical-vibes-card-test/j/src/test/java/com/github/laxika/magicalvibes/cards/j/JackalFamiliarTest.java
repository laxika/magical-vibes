package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JackalFamiliar.class, RuneclawBear.class})
class JackalFamiliarTest extends BaseCardTest {

    @Test
    @DisplayName("Jackal Familiar can't attack alone")
    void cantAttackAlone() {
        addCreatureReady(player1, new JackalFamiliar());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))

                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Jackal Familiar can attack with another creature")
    void canAttackWithAnother() {
        harness.setLife(player2, 20);

        addCreatureReady(player1, new JackalFamiliar());

        addCreatureReady(player1, new RuneclawBear());

        declareAttackers(List.of(0, 1));

        // Jackal Familiar (2/2) + Runeclaw Bear (2/2) = 4 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Two Jackal Familiars can attack together")
    void twoFamiliarsCanAttackTogether() {
        harness.setLife(player2, 20);

        addCreatureReady(player1, new JackalFamiliar());

        addCreatureReady(player1, new JackalFamiliar());

        declareAttackers(List.of(0, 1));

        // Two Jackal Familiars (2/2 each) = 4 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Jackal Familiar not included in available attackers when it's the only eligible creature")
    void notInAvailableAttackersWhenAlone() {
        addCreatureReady(player1, new JackalFamiliar());

        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId())).isEmpty();
        declareAttackers(List.of());
    }

    @Test
    @DisplayName("Jackal Familiar can't block alone")
    void cantBlockAlone() {
        Permanent attacker = addCreatureReady(player1, new RuneclawBear());
        attacker.setAttacking(true);

        addCreatureReady(player2, new JackalFamiliar());

        prepareDeclareBlockers();

        // The creature is filtered from available blockers when alone, so declaring it throws
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Jackal Familiar can block with another creature")
    void canBlockWithAnother() {
        Permanent attacker1 = addCreatureReady(player1, new RuneclawBear());
        attacker1.setAttacking(true);

        Permanent attacker2 = addCreatureReady(player1, new RuneclawBear());
        attacker2.setAttacking(true);

        Permanent familiar = addCreatureReady(player2, new JackalFamiliar());

        Permanent bears = addCreatureReady(player2, new RuneclawBear());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1)
        ));

        assertThat(familiar.isBlocking()).isTrue();
        assertThat(bears.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("An idle eligible creature does not let Jackal Familiar attack alone")
    void cantAttackAloneWithIdleCompanion() {
        addCreatureReady(player1, new JackalFamiliar());
        addCreatureReady(player1, new RuneclawBear());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An idle eligible creature does not let Jackal Familiar block alone")
    void cantBlockAloneWithIdleCompanion() {
        Permanent attacker = addCreatureReady(player1, new RuneclawBear());
        attacker.setAttacking(true);
        addCreatureReady(player2, new JackalFamiliar());
        addCreatureReady(player2, new RuneclawBear());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Two Jackal Familiars can block the same attacker together")
    void twoFamiliarsCanBlockTogether() {
        Permanent attacker = addCreatureReady(player1, new RuneclawBear());
        attacker.setAttacking(true);
        Permanent first = addCreatureReady(player2, new JackalFamiliar());
        Permanent second = addCreatureReady(player2, new JackalFamiliar());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A tapped companion cannot enable Jackal Familiar to attack")
    void tappedCompanionCannotEnableAttack() {
        addCreatureReady(player1, new JackalFamiliar());
        Permanent companion = addCreatureReady(player1, new RuneclawBear());
        companion.tap();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped companion cannot enable Jackal Familiar to block")
    void tappedCompanionCannotEnableBlock() {
        Permanent attacker = addCreatureReady(player1, new RuneclawBear());
        attacker.setAttacking(true);
        addCreatureReady(player2, new JackalFamiliar());
        Permanent companion = addCreatureReady(player2, new RuneclawBear());
        companion.tap();
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }
}
