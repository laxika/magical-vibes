package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GiantSpider.class, WindDrake.class, GrizzlyBears.class})
class GiantSpiderTest extends BaseCardTest {

    @Test
    void reachAllowsBlockingFlyingCreature() {
        addCreatureReady(player1, new WindDrake());
        Permanent spider = addCreatureReady(player2, new GiantSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(spider.isBlocking()).isTrue();
    }

    @Test
    void reachDoesNotRestrictBlockingNonFlyingCreature() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent spider = addCreatureReady(player2, new GiantSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(spider.isBlocking()).isTrue();
    }

    @Test
    void reachDoesNotPreventNonFlyingCreatureFromBlockingSpider() {
        addCreatureReady(player1, new GiantSpider());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(bears.isBlocking()).isTrue();
    }

    @Test
    void tappedSpiderCannotBlockFlyingCreature() {
        addCreatureReady(player1, new WindDrake());
        Permanent spider = addCreatureReady(player2, new GiantSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        spider.setTapped(true);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
        assertThat(spider.isBlocking()).isFalse();
    }
}
