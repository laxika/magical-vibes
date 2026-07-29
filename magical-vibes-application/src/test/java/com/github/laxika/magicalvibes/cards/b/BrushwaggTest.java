package com.github.laxika.magicalvibes.cards.b;

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

class BrushwaggTest extends BaseCardTest {

    @Test
    @DisplayName("When Brushwagg becomes blocked, it gets -2/+2 until end of turn")
    void becomesBlockedGetsBoost() {
        Permanent brushwagg = addReadyBrushwagg(player1);
        brushwagg.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(brushwagg.getPowerModifier()).isEqualTo(-2);
        assertThat(brushwagg.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("When Brushwagg blocks, it gets -2/+2 until end of turn")
    void blocksGetsBoost() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent brushwagg = addReadyBrushwagg(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(brushwagg.getPowerModifier()).isEqualTo(-2);
        assertThat(brushwagg.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("When Brushwagg is unblocked, it gets no boost")
    void unblockedNoBoost() {
        Permanent brushwagg = addReadyBrushwagg(player1);
        brushwagg.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(brushwagg.getPowerModifier()).isZero();
        assertThat(brushwagg.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOff() {
        Permanent brushwagg = addReadyBrushwagg(player1);
        brushwagg.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(brushwagg.getPowerModifier()).isZero();
        assertThat(brushwagg.getToughnessModifier()).isZero();
    }

    private Permanent addReadyBrushwagg(Player player) {
        Permanent permanent = new Permanent(new Brushwagg());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
