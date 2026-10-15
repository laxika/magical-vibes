package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.PutMinusOneCounterAtEndOfCombat;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WickerWarcrawler.class, GrizzlyBears.class})
class WickerWarcrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking gives it a -1/-1 counter by the end of combat")
    void attackingPutsCounterAtEndOfCombat() {
        Permanent crawler = addCreatureReady(player1, new WickerWarcrawler());

        declareAttackers(player1, List.of(0));
        // No blockers exist, so priorities cascade through the combat damage step and out of
        // end of combat, draining the scheduled counter.
        harness.passBothPriorities();

        assertThat(crawler.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, crawler)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, crawler)).isEqualTo(5);
    }

    @Test
    @DisplayName("Blocking puts a -1/-1 counter on it at end of combat")
    void blockingPutsCounterAtEndOfCombat() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent crawler = addCreatureReady(player2, new WickerWarcrawler());

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities(); // resolve the block trigger

        assertThat(crawler.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();

        leaveEndOfCombat();

        assertThat(crawler.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does nothing when it neither attacks nor blocks")
    void noCounterWhenNotInCombat() {
        Permanent crawler = addCreatureReady(player1, new WickerWarcrawler());

        declareAttackers(player1, List.of()); // stays back
        harness.passBothPriorities();

        assertThat(gd.hasDelayedAction(PutMinusOneCounterAtEndOfCombat.class)).isFalse();

        leaveEndOfCombat();

        assertThat(crawler.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The attack trigger schedules the counter without weakening combat damage")
    void attackTriggerDoesNotImmediatelyPutCounter() {
        Permanent crawler = addCreatureReady(player1, new WickerWarcrawler());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            assertThat(gd.stack).hasSize(1);
            resolveAllTriggers();

            assertThat(crawler.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        });
    }

    @Test
    @DisplayName("The counter uses a respondable delayed trigger at the beginning of end of combat")
    void counterTriggersAsEndOfCombatBegins() {
        Permanent crawler = addCreatureReady(player1, new WickerWarcrawler());
        harness.setLife(player2, 20);

        harness.withAutoStop(TurnStep.END_OF_COMBAT, () -> {
            declareAttackers(player1, List.of(0));
            harness.passUntil(player1, TurnStep.END_OF_COMBAT);

            harness.assertLife(player2, 14);
            assertThat(crawler.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
            assertThat(gd.stack).hasSize(1);

            resolveAllTriggers();

            assertThat(gd.currentStep).isEqualTo(TurnStep.END_OF_COMBAT);
            assertThat(crawler.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        });
    }

    private void leaveEndOfCombat() {
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
