package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WatcherSliver.class, BonescytheSliver.class, GrizzlyBears.class})
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
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());
        int basePower = gqs.getEffectivePower(gd, otherSliver);
        int baseToughness = gqs.getEffectiveToughness(gd, otherSliver);

        addCreatureReady(player1, new WatcherSliver());

        assertThat(gqs.getEffectivePower(gd, otherSliver)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, otherSliver)).isEqualTo(baseToughness + 2);
    }

    @Test
    @DisplayName("Watcher Sliver boosts an opponent's Sliver")
    void boostsOpponentSliver() {
        Permanent opponentSliver = addCreatureReady(player2, new BonescytheSliver());
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
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }
}
