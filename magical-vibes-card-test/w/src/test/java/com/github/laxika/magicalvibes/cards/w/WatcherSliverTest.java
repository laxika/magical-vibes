package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BenalishCavalry;
import com.github.laxika.magicalvibes.cards.s.SidewinderSliver;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WatcherSliver.class, SidewinderSliver.class, BenalishCavalry.class})
class WatcherSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Watcher Sliver boosts itself")
    void boostsSelf() {
        Permanent watcherSliver = addCreatureReady(player1, new WatcherSliver());

        assertThat(gqs.getEffectivePower(gd, watcherSliver)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, watcherSliver)).isEqualTo(4);
    }

    @Test
    @DisplayName("Watcher Sliver boosts another Sliver you control")
    void boostsAnotherSliver() {
        Permanent otherSliver = addCreatureReady(player1, new SidewinderSliver());
        int basePower = gqs.getEffectivePower(gd, otherSliver);
        int baseToughness = gqs.getEffectiveToughness(gd, otherSliver);

        addCreatureReady(player1, new WatcherSliver());

        assertThat(gqs.getEffectivePower(gd, otherSliver)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, otherSliver)).isEqualTo(baseToughness + 2);
    }

    @Test
    @DisplayName("Watcher Sliver boosts an opponent's Sliver")
    void boostsOpponentSliver() {
        Permanent opponentSliver = addCreatureReady(player2, new SidewinderSliver());
        int basePower = gqs.getEffectivePower(gd, opponentSliver);
        int baseToughness = gqs.getEffectiveToughness(gd, opponentSliver);

        addCreatureReady(player1, new WatcherSliver());

        assertThat(gqs.getEffectivePower(gd, opponentSliver)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, opponentSliver)).isEqualTo(baseToughness + 2);
    }

    @Test
    @DisplayName("Watcher Sliver does not boost a non-Sliver creature")
    void doesNotBoostNonSliver() {
        addCreatureReady(player1, new WatcherSliver());
        Permanent cavalry = addCreatureReady(player1, new BenalishCavalry());

        assertThat(gqs.getEffectivePower(gd, cavalry)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cavalry)).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple Watcher Slivers stack across both battlefields")
    void multipleWatchersStack() {
        Permanent friendlyWatcher = addCreatureReady(player1, new WatcherSliver());
        Permanent opposingWatcher = addCreatureReady(player2, new WatcherSliver());
        Permanent lateSliver = addCreatureReady(player2, new SidewinderSliver());
        Permanent nonSliver = addCreatureReady(player2, new BenalishCavalry());

        assertThat(gqs.getEffectivePower(gd, friendlyWatcher)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, friendlyWatcher)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, opposingWatcher)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingWatcher)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, lateSliver)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, lateSliver)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, nonSliver)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonSliver)).isEqualTo(2);
    }

    @Test
    @DisplayName("Watcher Sliver's bonus ends immediately when it leaves the battlefield")
    void bonusEndsWhenWatcherLeaves() {
        Permanent watcher = addCreatureReady(player1, new WatcherSliver());
        Permanent friendlySliver = addCreatureReady(player1, new SidewinderSliver());
        Permanent opposingSliver = addCreatureReady(player2, new SidewinderSliver());

        assertThat(gqs.getEffectiveToughness(gd, friendlySliver)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opposingSliver)).isEqualTo(3);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, watcher));

        harness.assertInHand(player1, "Watcher Sliver");
        harness.assertNotOnBattlefield(player1, "Watcher Sliver");
        assertThat(gqs.getEffectivePower(gd, friendlySliver)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, friendlySliver)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opposingSliver)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingSliver)).isEqualTo(1);
    }
}
