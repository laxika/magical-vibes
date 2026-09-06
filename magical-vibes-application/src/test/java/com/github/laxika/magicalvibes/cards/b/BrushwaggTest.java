package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.w.WildElephant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Brushwagg.class, WildElephant.class})
class BrushwaggTest extends BaseCardTest {

    @Test
    @DisplayName("When Brushwagg becomes blocked, it gets -2/+2 until end of turn")
    void becomesBlockedGetsBoost() {
        Permanent brushwagg = addCreatureReady(player1, new Brushwagg());
        brushwagg.setAttacking(true);
        addCreatureReady(player2, new WildElephant());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(brushwagg.getPowerModifier()).isEqualTo(-2);
        assertThat(brushwagg.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("When Brushwagg blocks, it gets -2/+2 until end of turn")
    void blocksGetsBoost() {
        Permanent attacker = addCreatureReady(player1, new WildElephant());
        attacker.setAttacking(true);
        Permanent brushwagg = addCreatureReady(player2, new Brushwagg());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(brushwagg.getPowerModifier()).isEqualTo(-2);
        assertThat(brushwagg.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("When Brushwagg is unblocked, it gets no boost")
    void unblockedNoBoost() {
        Permanent brushwagg = addCreatureReady(player1, new Brushwagg());
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
        Permanent brushwagg = addCreatureReady(player1, new Brushwagg());
        brushwagg.setAttacking(true);
        addCreatureReady(player2, new WildElephant());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(brushwagg.getPowerModifier()).isZero();
        assertThat(brushwagg.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("When Brushwagg becomes blocked by multiple creatures, it gets only one boost")
    void becomesBlockedByMultipleCreaturesGetsOneBoost() {
        Permanent brushwagg = addCreatureReady(player1, new Brushwagg());
        brushwagg.setAttacking(true);
        addCreatureReady(player2, new WildElephant());
        addCreatureReady(player2, new WildElephant());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(brushwagg.getPowerModifier()).isEqualTo(-2);
        assertThat(brushwagg.getToughnessModifier()).isEqualTo(2);
    }
}
