package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.SteadfastCathar;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WildwoodTracker.class, GrizzlyBears.class, LlanowarElves.class, SteadfastCathar.class})
class WildwoodTrackerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with another non-Human creature gives Wildwood Tracker +1/+1")
    void attackWithAnotherNonHumanCreatureBoostsTracker() {
        Permanent tracker = addCreatureReady(player1, new WildwoodTracker());
        addCreatureReady(player1, new LlanowarElves());

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(tracker.getEffectivePower()).isEqualTo(2);
        assertThat(tracker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("A Human creature does not satisfy Wildwood Tracker's condition")
    void humanCreatureDoesNotBoostTracker() {
        Permanent tracker = addCreatureReady(player1, new WildwoodTracker());
        addCreatureReady(player1, new SteadfastCathar());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(tracker.getEffectivePower()).isEqualTo(1);
        assertThat(tracker.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Blocking with another non-Human creature gives Wildwood Tracker +1/+1")
    void blockWithAnotherNonHumanCreatureBoostsTracker() {
        Permanent attacker = addCreatureReady(player1, new LlanowarElves());
        attacker.setAttacking(true);
        Permanent tracker = addCreatureReady(player2, new WildwoodTracker());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(tracker.getEffectivePower()).isEqualTo(2);
        assertThat(tracker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("A Human creature does not satisfy Wildwood Tracker when blocking")
    void humanCreatureDoesNotBoostTrackerWhenBlocking() {
        Permanent attacker = addCreatureReady(player1, new LlanowarElves());
        attacker.setAttacking(true);
        Permanent tracker = addCreatureReady(player2, new WildwoodTracker());
        addCreatureReady(player2, new SteadfastCathar());

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(tracker.getEffectivePower()).isEqualTo(1);
        assertThat(tracker.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Wildwood Tracker's boost lasts until end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent tracker = addCreatureReady(player1, new WildwoodTracker());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        assertThat(tracker.getEffectivePower()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(tracker.getEffectivePower()).isEqualTo(1);
        assertThat(tracker.getEffectiveToughness()).isEqualTo(1);
    }
}
