package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.action.TapAndSkipUntapAtEndOfCombat;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JovensFerretsTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking gives Joven's Ferrets +0/+2 until end of turn")
    void attackTriggerBoostsToughness() {
        Permanent ferrets = addCreatureReady(player1, new JovensFerrets());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(ferrets.getPowerModifier()).isZero();
        assertThat(ferrets.getToughnessModifier()).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ferrets)).isEqualTo(3);
    }

    @Test
    @DisplayName("Becoming blocked schedules the blocker to be tapped at end of combat")
    void becomingBlockedSchedulesTap() {
        Permanent ferrets = addCreatureReady(player1, new JovensFerrets());
        ferrets.setAttacking(true);
        Permanent spider = addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Joven's Ferrets")
                        && spider.getId().equals(e.getTargetId()));

        harness.passBothPriorities();
        assertThat(gd.getDelayedActions(TapAndSkipUntapAtEndOfCombat.class))
                .anyMatch(a -> a.permanentId().equals(spider.getId()));
    }

    @Test
    @DisplayName("The blocker is tapped at end of combat and skips its next untap step")
    void blockerTappedAndUntapLockedAtEndOfCombat() {
        Permanent ferrets = addCreatureReady(player1, new JovensFerrets());
        ferrets.setAttacking(true);
        Permanent spider = addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(spider.isTapped()).isTrue();
        assertThat(spider.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Nothing is scheduled when Joven's Ferrets goes unblocked")
    void nothingScheduledWhenUnblocked() {
        Permanent ferrets = addCreatureReady(player1, new JovensFerrets());
        ferrets.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.getDelayedActions(TapAndSkipUntapAtEndOfCombat.class)).isEmpty();
    }
}
