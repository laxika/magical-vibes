package com.github.laxika.magicalvibes.cards.d;

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

class DeepwoodWolverineTest extends BaseCardTest {

    @Test
    @DisplayName("When Deepwood Wolverine becomes blocked, it gets +2/+0 until end of turn")
    void becomesBlockedGetsBoost() {
        Permanent wolverine = addReadyWolverine(player1);
        wolverine.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(wolverine.getPowerModifier()).isEqualTo(2);
        assertThat(wolverine.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Deepwood Wolverine gets no boost when it is unblocked")
    void unblockedGetsNoBoost() {
        Permanent wolverine = addReadyWolverine(player1);
        wolverine.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(wolverine.getPowerModifier()).isZero();
        assertThat(wolverine.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The becomes-blocked trigger fires only once with multiple blockers")
    void becomesBlockedFiresOnceWithMultipleBlockers() {
        Permanent wolverine = addReadyWolverine(player1);
        wolverine.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();

        assertThat(wolverine.getPowerModifier()).isEqualTo(2);
        assertThat(wolverine.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent wolverine = addReadyWolverine(player1);
        wolverine.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(wolverine.getPowerModifier()).isZero();
        assertThat(wolverine.getToughnessModifier()).isZero();
    }

    private Permanent addReadyWolverine(Player player) {
        Permanent permanent = new Permanent(new DeepwoodWolverine());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
