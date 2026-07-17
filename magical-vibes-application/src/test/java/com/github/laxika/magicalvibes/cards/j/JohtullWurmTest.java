package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JohtullWurmTest extends BaseCardTest {

    @Test
    @DisplayName("With a single blocker the wurm is unaffected")
    void oneBlockerNoPenalty() {
        Permanent wurm = addReadyWurm(player1);
        wurm.setAttacking(true);
        addReadyBears(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(wurm.getPowerModifier()).isZero();
        assertThat(wurm.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("With two blockers the wurm gets -2/-1 until end of turn")
    void twoBlockersMinusTwoMinusOne() {
        Permanent wurm = addReadyWurm(player1);
        wurm.setAttacking(true);
        addReadyBears(player2);
        addReadyBears(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();

        assertThat(wurm.getPowerModifier()).isEqualTo(-2);
        assertThat(wurm.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("With three blockers the penalty scales to -4/-2")
    void threeBlockersMinusFourMinusTwo() {
        Permanent wurm = addReadyWurm(player1);
        wurm.setAttacking(true);
        addReadyBears(player2);
        addReadyBears(player2);
        addReadyBears(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)
        ));
        harness.passBothPriorities();

        assertThat(wurm.getPowerModifier()).isEqualTo(-4);
        assertThat(wurm.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("If unblocked no penalty is applied")
    void unblockedNoPenalty() {
        Permanent wurm = addReadyWurm(player1);
        wurm.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(wurm.getPowerModifier()).isZero();
        assertThat(wurm.getToughnessModifier()).isZero();
    }

    private Permanent addReadyWurm(Player player) {
        Permanent permanent = new Permanent(new JohtullWurm());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addReadyBears(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
    }

    private void prepareDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
