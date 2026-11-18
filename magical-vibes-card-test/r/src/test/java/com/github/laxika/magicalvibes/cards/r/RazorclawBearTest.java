package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RazorclawBear.class})
class RazorclawBearTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming blocked creates one becomes-blocked trigger")
    void becomingBlockedCreatesTrigger() {
        Permanent bear = addCreatureReady(player1, new RazorclawBear());
        addCreatureReady(player2, new RazorclawBear());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getSourcePermanentId()).isEqualTo(bear.getId());
    }

    @Test
    @DisplayName("When blocked Razorclaw Bear gets +2/+2 until end of turn")
    void blockedGivesPlusTwoPlusTwo() {
        Permanent bear = addCreatureReady(player1, new RazorclawBear());
        addCreatureReady(player2, new RazorclawBear());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, bear)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(5);
    }

    @Test
    @DisplayName("Blocked by two creatures still only gets +2/+2")
    void twoBlockersStillPlusTwoPlusTwo() {
        Permanent bear = addCreatureReady(player1, new RazorclawBear());
        addCreatureReady(player2, new RazorclawBear());
        addCreatureReady(player2, new RazorclawBear());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, bear)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("If unblocked no becomes-blocked trigger is created")
    void unblockedCreatesNoTrigger() {
        Permanent bear = addCreatureReady(player1, new RazorclawBear());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(PermanentGrants.powerBoostOf(gd, bear)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, bear)).isZero();
    }

    @Test
    @DisplayName("The becomes-blocked boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent bear = addCreatureReady(player1, new RazorclawBear());
        addCreatureReady(player2, new RazorclawBear());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, bear)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, bear)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, bear)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, bear)).isZero();
    }

    @Test
    @DisplayName("Each blocked Bear boosts only itself after its trigger resolves")
    void multipleBlockedBearsBoostIndependently() {
        Permanent firstBear = addCreatureReady(player1, new RazorclawBear());
        Permanent secondBear = addCreatureReady(player1, new RazorclawBear());
        Permanent firstBlocker = addCreatureReady(player2, new RazorclawBear());
        Permanent secondBlocker = addCreatureReady(player2, new RazorclawBear());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1)
        ));

        assertThat(gd.stack).hasSize(2);
        assertThat(PermanentGrants.powerBoostOf(gd, firstBear)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, firstBear)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, secondBear)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, secondBear)).isZero();

        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, firstBear)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, firstBear)).isEqualTo(2);
        assertThat(PermanentGrants.powerBoostOf(gd, secondBear)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, secondBear)).isEqualTo(2);
        assertThat(PermanentGrants.powerBoostOf(gd, firstBlocker)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, firstBlocker)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, secondBlocker)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, secondBlocker)).isZero();
    }
}
