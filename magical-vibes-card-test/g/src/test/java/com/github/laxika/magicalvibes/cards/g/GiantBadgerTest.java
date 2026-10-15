package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HighGround;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GiantBadger.class, GrizzlyBears.class, HighGround.class})
class GiantBadgerTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking triggers +2/+2 until end of turn")
    void blockingTriggersBoost() {
        Permanent badger = addCreatureReady(player2, new GiantBadger());
        addCreatureReady(player1, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // Block trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        assertThat(badger.getPowerModifier()).isEqualTo(2);
        assertThat(badger.getToughnessModifier()).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, badger)).isEqualTo(4);   // 2 base + 2
        assertThat(gqs.getEffectiveToughness(gd, badger)).isEqualTo(4); // 2 base + 2
    }

    @Test
    @DisplayName("Boost resets at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent badger = addCreatureReady(player2, new GiantBadger());
        addCreatureReady(player1, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(badger.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(badger.getPowerModifier()).isEqualTo(0);
        assertThat(gqs.getEffectivePower(gd, badger)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger when it does not block")
    void doesNotTriggerWithoutBlocking() {
        Permanent badger = addCreatureReady(player2, new GiantBadger());
        addCreatureReady(player1, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(badger.getPowerModifier()).isZero();
        assertThat(badger.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Blocking multiple creatures triggers only once")
    void blockingMultipleCreaturesTriggersOnce() {
        harness.addToBattlefield(player2, new HighGround());
        Permanent badger = addCreatureReady(player2, new GiantBadger());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(1, 0),
                new BlockerAssignment(1, 1)));
        assertThat(gd.stack.stream()
                .filter(entry -> badger.getId().equals(entry.getSourcePermanentId())))
                .hasSize(1);
        resolveAllTriggers();

        assertThat(badger.getPowerModifier()).isEqualTo(2);
        assertThat(badger.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The blocking boost waits for its trigger to resolve")
    void boostWaitsForResolution() {
        Permanent badger = addCreatureReady(player2, new GiantBadger());
        addCreatureReady(player1, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        assertThat(badger.getPowerModifier()).isZero();
        assertThat(badger.getToughnessModifier()).isZero();

        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, badger)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, badger)).isEqualTo(4);
    }

    @Test
    @DisplayName("An attacking Badger does not get a boost when it becomes blocked")
    void becomingBlockedDoesNotTriggerBoost() {
        Permanent badger = addCreatureReady(player1, new GiantBadger());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(badger.getPowerModifier()).isZero();
        assertThat(badger.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Only the Badger that blocks receives the boost")
    void boostAppliesOnlyToBlockingBadger() {
        Permanent blocker = addCreatureReady(player2, new GiantBadger());
        Permanent nonBlocker = addCreatureReady(player2, new GiantBadger());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(4);
        assertThat(nonBlocker.getPowerModifier()).isZero();
        assertThat(nonBlocker.getToughnessModifier()).isZero();
        assertThat(attacker.getPowerModifier()).isZero();
        assertThat(attacker.getToughnessModifier()).isZero();
    }
}
