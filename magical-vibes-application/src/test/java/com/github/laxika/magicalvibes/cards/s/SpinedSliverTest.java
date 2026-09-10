package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HibernationSliver;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpinedSliver.class, SpinedWurm.class, HibernationSliver.class})
class SpinedSliverTest extends BaseCardTest {

    @Test
    @DisplayName("With one blocker Spined Sliver gets +1/+1 until end of turn")
    void oneBlockerGivesPlusOnePlusOne() {
        Permanent sliver = addCreatureReady(player1, new SpinedSliver());
        sliver.setAttacking(true);
        addCreatureReady(player2, new SpinedWurm());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(sliver.getPowerModifier()).isEqualTo(1);
        assertThat(sliver.getToughnessModifier()).isEqualTo(1);
        assertThat(sliver.getEffectivePower()).isEqualTo(3);
        assertThat(sliver.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("With two blockers Spined Sliver gets +2/+2 until end of turn")
    void twoBlockersGivesPlusTwoPlusTwo() {
        Permanent sliver = addCreatureReady(player1, new SpinedSliver());
        sliver.setAttacking(true);
        addCreatureReady(player2, new SpinedWurm());
        addCreatureReady(player2, new SpinedWurm());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();

        assertThat(sliver.getPowerModifier()).isEqualTo(2);
        assertThat(sliver.getToughnessModifier()).isEqualTo(2);
        assertThat(sliver.getEffectivePower()).isEqualTo(4);
        assertThat(sliver.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Spined Sliver boosts a Sliver controlled by the other player")
    void boostsOpponentsSliver() {
        Permanent sliver = addCreatureReady(player1, new HibernationSliver());
        sliver.setAttacking(true);
        addCreatureReady(player2, new SpinedSliver());
        addCreatureReady(player2, new SpinedWurm());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThat(sliver.getPowerModifier()).isEqualTo(1);
        assertThat(sliver.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Spined Sliver does not trigger for a blocked non-Sliver")
    void doesNotBoostBlockedNonSliver() {
        Permanent wurm = addCreatureReady(player1, new SpinedWurm());
        wurm.setAttacking(true);
        addCreatureReady(player2, new SpinedSliver());
        addCreatureReady(player2, new SpinedWurm());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(wurm.getPowerModifier()).isZero();
        assertThat(wurm.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("If unblocked Spined Sliver gets no boost")
    void unblockedGetsNoBoost() {
        Permanent sliver = addCreatureReady(player1, new SpinedSliver());
        sliver.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(sliver.getPowerModifier()).isZero();
        assertThat(sliver.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Spined Sliver's boost wears off at the end of the turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent sliver = addCreatureReady(player1, new SpinedSliver());
        sliver.setAttacking(true);
        addCreatureReady(player2, new SpinedWurm());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(sliver.getPowerModifier()).isEqualTo(1);
        assertThat(sliver.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(sliver.getPowerModifier()).isZero();
        assertThat(sliver.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The boost resolves after the Spined Sliver leaves the battlefield")
    void boostResolvesAfterSourceLeavesBattlefield() {
        Permanent sliver = addCreatureReady(player1, new HibernationSliver());
        sliver.setAttacking(true);
        Permanent source = addCreatureReady(player2, new SpinedSliver());
        addCreatureReady(player2, new SpinedWurm());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));
        gd.playerBattlefields.get(player2.getId()).remove(source);
        harness.passBothPriorities();

        assertThat(sliver.getPowerModifier()).isEqualTo(1);
        assertThat(sliver.getToughnessModifier()).isEqualTo(1);
    }
}
