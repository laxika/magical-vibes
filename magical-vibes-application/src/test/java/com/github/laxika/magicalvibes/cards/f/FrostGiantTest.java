package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.z.ZephyrFalcon;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FrostGiant.class, ZephyrFalcon.class})
class FrostGiantTest extends BaseCardTest {

    @Test
    @DisplayName("With one blocker Rampage 2 grants no bonus")
    void oneBlockerGivesNothing() {
        Permanent giant = addCreatureReady(player1, new FrostGiant());
        addCreatureReady(player2, new ZephyrFalcon());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(giant.getPowerModifier()).isZero();
        assertThat(giant.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("With two blockers Rampage 2 grants +2/+2 until end of turn")
    void twoBlockersGivesPlusTwo() {
        Permanent giant = addCreatureReady(player1, new FrostGiant());
        addCreatureReady(player2, new ZephyrFalcon());
        addCreatureReady(player2, new ZephyrFalcon());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();

        assertThat(giant.getPowerModifier()).isEqualTo(2);
        assertThat(giant.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("With three blockers Rampage 2 grants +4/+4 until end of turn")
    void threeBlockersGivesPlusFour() {
        Permanent giant = addCreatureReady(player1, new FrostGiant());
        addCreatureReady(player2, new ZephyrFalcon());
        addCreatureReady(player2, new ZephyrFalcon());
        addCreatureReady(player2, new ZephyrFalcon());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)
        ));
        harness.passBothPriorities();

        assertThat(giant.getPowerModifier()).isEqualTo(4);
        assertThat(giant.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("If unblocked no becomes-blocked trigger is created")
    void unblockedCreatesNoTrigger() {
        Permanent giant = addCreatureReady(player1, new FrostGiant());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(giant.getPowerModifier()).isZero();
        assertThat(giant.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The rampage bonus wears off at end of turn")
    void rampageBonusWearsOffAtEndOfTurn() {
        Permanent giant = addCreatureReady(player1, new FrostGiant());
        addCreatureReady(player2, new ZephyrFalcon());
        addCreatureReady(player2, new ZephyrFalcon());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();

        assertThat(giant.getPowerModifier()).isEqualTo(2);
        assertThat(giant.getToughnessModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(giant.getPowerModifier()).isZero();
        assertThat(giant.getToughnessModifier()).isZero();
    }
}
