package com.github.laxika.magicalvibes.cards.f;

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

        declareAttackers(List.of(0));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.isNonTargeting()).isTrue();

        harness.passBothPriorities();

        Permanent boosted = findPermanent(player1, "Trained Armodon");
        assertThat(boosted.getPowerModifier()).isEqualTo(1);
        assertThat(boosted.getToughnessModifier()).isEqualTo(1);
        assertThat(drake.getPowerModifier()).isZero();
        assertThat(drake.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Each blocking Flailing Drake gives the attacker +1/+1")
    void eachBlockingDrakeBoostsAttacker() {
        Permanent firstDrake = addCreatureReady(player2, new FlailingDrake());
        Permanent secondDrake = addCreatureReady(player2, new FlailingDrake());
        addCreatureReady(player1, new TrainedArmodon());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(gd.stack).hasSize(2);

        resolveAllTriggers();

        Permanent boosted = findPermanent(player1, "Trained Armodon");
        assertThat(boosted.getPowerModifier()).isEqualTo(2);
        assertThat(boosted.getToughnessModifier()).isEqualTo(2);
        assertThat(firstDrake.getPowerModifier()).isZero();
        assertThat(firstDrake.getToughnessModifier()).isZero();
        assertThat(secondDrake.getPowerModifier()).isZero();
        assertThat(secondDrake.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Becoming blocked gives the blocker +1/+1 until end of turn")
    void becomingBlockedBoostsBlocker() {
        Permanent drake = addCreatureReady(player1, new FlailingDrake());
        Permanent blocker = addCreatureReady(player2, new WindDrake());

        declareAttackers(List.of(0));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getTargetId()).isEqualTo(blocker.getId());
        assertThat(entry.getSourcePermanentId()).isEqualTo(drake.getId());

        harness.passBothPriorities();

        Permanent boosted = findPermanent(player2, "Wind Drake");
        assertThat(boosted.getPowerModifier()).isEqualTo(1);
        assertThat(boosted.getToughnessModifier()).isEqualTo(1);
        assertThat(drake.getPowerModifier()).isZero();
        assertThat(drake.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Becoming blocked by multiple creatures boosts each blocker")
    void becomingBlockedByMultipleCreaturesBoostsEach() {
        Permanent drake = addCreatureReady(player1, new FlailingDrake());
        addCreatureReady(player2, new WindDrake());
        addCreatureReady(player2, new WindDrake());

        declareAttackers(List.of(0));

        prepareDeclareBlockers();
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
        assertThat(blockers).allMatch(p -> p.getPowerModifier() == 1 && p.getToughnessModifier() == 1);
        assertThat(drake.getPowerModifier()).isZero();
        assertThat(drake.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent drake = addCreatureReady(player1, new FlailingDrake());
        addCreatureReady(player2, new WindDrake());

        declareAttackers(List.of(0));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent blocker = findPermanent(player2, "Wind Drake");
        assertThat(blocker.getPowerModifier()).isZero();
        assertThat(blocker.getToughnessModifier()).isZero();
    }
}
