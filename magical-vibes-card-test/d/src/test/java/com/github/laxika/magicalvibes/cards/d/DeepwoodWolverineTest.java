package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.c.CrenellatedWall;
import com.github.laxika.magicalvibes.cards.h.HuntedWumpus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeepwoodWolverine.class, CrenellatedWall.class, HuntedWumpus.class})
class DeepwoodWolverineTest extends BaseCardTest {

    @Test
    @DisplayName("When Deepwood Wolverine becomes blocked, it gets +2/+0 until end of turn")
    void becomesBlockedGetsBoost() {
        Permanent wolverine = addCreatureReady(player1, new DeepwoodWolverine());
        Permanent blocker = addCreatureReady(player2, new CrenellatedWall());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, wolverine)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, wolverine)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, blocker)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, blocker)).isZero();
    }

    @Test
    @DisplayName("Deepwood Wolverine gets no boost when it is unblocked")
    void unblockedGetsNoBoost() {
        Permanent wolverine = addCreatureReady(player1, new DeepwoodWolverine());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());

        assertThat(PermanentGrants.powerBoostOf(gd, wolverine)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, wolverine)).isZero();
    }

    @Test
    @DisplayName("Blocking another creature does not trigger Deepwood Wolverine's ability")
    void blockingDoesNotTriggerAbility() {
        addCreatureReady(player1, new HuntedWumpus());
        Permanent wolverine = addCreatureReady(player2, new DeepwoodWolverine());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(PermanentGrants.powerBoostOf(gd, wolverine)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, wolverine)).isZero();
    }

    @Test
    @DisplayName("The becomes-blocked trigger fires only once with multiple blockers")
    void becomesBlockedFiresOnceWithMultipleBlockers() {
        Permanent wolverine = addCreatureReady(player1, new DeepwoodWolverine());
        addCreatureReady(player2, new CrenellatedWall());
        addCreatureReady(player2, new CrenellatedWall());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, wolverine)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, wolverine)).isZero();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent wolverine = addCreatureReady(player1, new DeepwoodWolverine());
        addCreatureReady(player2, new CrenellatedWall());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, wolverine)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, wolverine)).isZero();
    }

    @Test
    @DisplayName("The boost waits for resolution and affects only the Wolverine that became blocked")
    void boostResolvesOnlyForBlockedWolverine() {
        Permanent blocked = addCreatureReady(player1, new DeepwoodWolverine());
        Permanent unblocked = addCreatureReady(player1, new DeepwoodWolverine());
        addCreatureReady(player2, new CrenellatedWall());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(PermanentGrants.powerBoostOf(gd, blocked)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, unblocked)).isZero();

        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, blocked)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, blocked)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, unblocked)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, unblocked)).isZero();
    }
}
