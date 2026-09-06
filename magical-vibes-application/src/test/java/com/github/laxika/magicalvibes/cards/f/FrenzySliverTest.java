package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FrenzySliver.class, BonescytheSliver.class, GrizzlyBears.class})
class FrenzySliverTest extends BaseCardTest {

    @Test
    @DisplayName("Frenzy Sliver gives an unblocked Sliver +1/+0 until end of turn")
    void unblockedSliverGetsBoost() {
        Permanent frenzySliver = addCreatureReady(player1, new FrenzySliver());
        frenzySliver.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(frenzySliver.getPowerModifier()).isEqualTo(1);
        assertThat(frenzySliver.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Frenzy Sliver gives another player's unblocked Sliver +1/+0")
    void grantsFrenzyToOpposingSliver() {
        addCreatureReady(player1, new FrenzySliver());
        Permanent opposingSliver = addCreatureReady(player2, new BonescytheSliver());
        opposingSliver.setAttacking(true);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of());
        harness.passBothPriorities();

        assertThat(opposingSliver.getPowerModifier()).isEqualTo(1);
        assertThat(opposingSliver.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A blocked Sliver does not get a frenzy boost")
    void blockedSliverGetsNoBoost() {
        Permanent frenzySliver = addCreatureReady(player1, new FrenzySliver());
        frenzySliver.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(frenzySliver.getPowerModifier()).isZero();
        assertThat(frenzySliver.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Frenzy Sliver does not grant the ability to non-Slivers")
    void nonSliverGetsNoBoost() {
        addCreatureReady(player1, new FrenzySliver());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Frenzy's temporary boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent frenzySliver = addCreatureReady(player1, new FrenzySliver());
        frenzySliver.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
        assertThat(frenzySliver.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(frenzySliver.getPowerModifier()).isZero();
        assertThat(frenzySliver.getToughnessModifier()).isZero();
    }
}
