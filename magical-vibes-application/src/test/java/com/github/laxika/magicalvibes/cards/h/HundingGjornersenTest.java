package com.github.laxika.magicalvibes.cards.h;

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

@CardUsed({HundingGjornersen.class, ZephyrFalcon.class})
class HundingGjornersenTest extends BaseCardTest {

    @Test
    @DisplayName("With one blocker Rampage 1 grants no bonus")
    void oneBlockerGivesNothing() {
        Permanent hunding = addHunding();
        addBlockers(1);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(hunding.getPowerModifier()).isZero();
        assertThat(hunding.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("With three blockers Rampage 1 grants +2/+2 until end of turn")
    void threeBlockersGivesPlusTwo() {
        Permanent hunding = addHunding();
        addBlockers(3);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)));
        harness.passBothPriorities();

        assertThat(hunding.getPowerModifier()).isEqualTo(2);
        assertThat(hunding.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("If unblocked no becomes-blocked trigger is created")
    void unblockedCreatesNoTrigger() {
        Permanent hunding = addHunding();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(hunding.getPowerModifier()).isZero();
        assertThat(hunding.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The rampage bonus wears off at end of turn")
    void rampageBonusWearsOffAtEndOfTurn() {
        Permanent hunding = addHunding();
        addBlockers(2);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThat(hunding.getPowerModifier()).isEqualTo(1);
        assertThat(hunding.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(hunding.getPowerModifier()).isZero();
        assertThat(hunding.getToughnessModifier()).isZero();
    }

    private Permanent addHunding() {
        return addCreatureReady(player1, new HundingGjornersen());
    }

    private void addBlockers(int count) {
        for (int i = 0; i < count; i++) {
            addCreatureReady(player2, new ZephyrFalcon());
        }
    }
}
