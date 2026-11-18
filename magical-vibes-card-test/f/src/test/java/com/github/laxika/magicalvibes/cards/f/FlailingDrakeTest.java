package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
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

@CardUsed({FlailingDrake.class, TrainedArmodon.class, WindDrake.class})
class FlailingDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking gives the attacker +1/+1 until end of turn")
    void blockingBoostsAttacker() {
        Permanent drake = addCreatureReady(player2, new FlailingDrake());
        addCreatureReady(player1, new TrainedArmodon());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.isNonTargeting()).isTrue();

        harness.passBothPriorities();

        Permanent boosted = findPermanent(player1, "Trained Armodon");
        assertThat(PermanentGrants.powerBoostOf(gd, boosted)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, boosted)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, drake)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, drake)).isZero();
    }

    @Test
    @DisplayName("Each blocking Flailing Drake gives the attacker +1/+1")
    void eachBlockingDrakeBoostsAttacker() {
        Permanent firstDrake = addCreatureReady(player2, new FlailingDrake());
        Permanent secondDrake = addCreatureReady(player2, new FlailingDrake());
        addCreatureReady(player1, new TrainedArmodon());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(gd.stack).hasSize(2);

        resolveAllTriggers();

        Permanent boosted = findPermanent(player1, "Trained Armodon");
        assertThat(PermanentGrants.powerBoostOf(gd, boosted)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, boosted)).isEqualTo(2);
        assertThat(PermanentGrants.powerBoostOf(gd, firstDrake)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, firstDrake)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, secondDrake)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, secondDrake)).isZero();
    }

    @Test
    @DisplayName("Becoming blocked gives the blocker +1/+1 until end of turn")
    void becomingBlockedBoostsBlocker() {
        Permanent drake = addCreatureReady(player1, new FlailingDrake());
        Permanent blocker = addCreatureReady(player2, new WindDrake());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getTargetId()).isEqualTo(blocker.getId());
        assertThat(entry.getSourcePermanentId()).isEqualTo(drake.getId());

        harness.passBothPriorities();

        Permanent boosted = findPermanent(player2, "Wind Drake");
        assertThat(PermanentGrants.powerBoostOf(gd, boosted)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, boosted)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, drake)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, drake)).isZero();
    }

    @Test
    @DisplayName("Becoming blocked by multiple creatures boosts each blocker")
    void becomingBlockedByMultipleCreaturesBoostsEach() {
        Permanent drake = addCreatureReady(player1, new FlailingDrake());
        addCreatureReady(player2, new WindDrake());
        addCreatureReady(player2, new WindDrake());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        long triggerCount = gd.stack.stream()
                .filter(e -> e.getCard().getName().equals("Flailing Drake"))
                .count();
        assertThat(triggerCount).isEqualTo(2);

        resolveAllTriggers();

        List<Permanent> blockers = findPermanents(player2, "Wind Drake");
        assertThat(blockers).hasSize(2);
        assertThat(blockers).allMatch(p -> PermanentGrants.powerBoostOf(gd, p) == 1 && PermanentGrants.toughnessBoostOf(gd, p) == 1);
        assertThat(PermanentGrants.powerBoostOf(gd, drake)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, drake)).isZero();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new FlailingDrake());
        addCreatureReady(player2, new WindDrake());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent blocker = findPermanent(player2, "Wind Drake");
        assertThat(PermanentGrants.powerBoostOf(gd, blocker)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, blocker)).isZero();
    }

    @Test
    @DisplayName("Two Flailing Drakes in combat each boost their opponent")
    void opposingDrakesBoostEachOther() {
        Permanent attacker = addCreatureReady(player1, new FlailingDrake());
        Permanent blocker = addCreatureReady(player2, new FlailingDrake());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allMatch(StackEntry::isNonTargeting);
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, blocker)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, blocker)).isEqualTo(1);
    }

    @Test
    @DisplayName("An unblocked attack does not trigger a boost")
    void unblockedAttackDoesNotBoostAnyCreature() {
        Permanent attacker = addCreatureReady(player1, new FlailingDrake());
        Permanent defender = addCreatureReady(player2, new WindDrake());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, defender)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, defender)).isZero();
    }
}
