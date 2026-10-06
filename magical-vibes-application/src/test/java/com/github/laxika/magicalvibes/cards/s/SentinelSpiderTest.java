package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SentinelSpider.class, SerraAngel.class})
class SentinelSpiderTest extends BaseCardTest {

    @Test
    void vigilanceKeepsSpiderUntappedWhileAttacking() {
        Permanent spider = addCreatureReady(player1, new SentinelSpider());
        addCreatureReady(player2, new SentinelSpider());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(spider.isAttacking()).isTrue();
        assertThat(spider.isTapped()).isFalse();
    }

    @Test
    void reachAllowsBlockingFlyingCreature() {
        addCreatureReady(player1, new SerraAngel());
        Permanent spider = addCreatureReady(player2, new SentinelSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(spider.isBlocking()).isTrue();
    }

    @Test
    void reachDoesNotPreventGroundCreatureFromBlockingSpider() {
        addCreatureReady(player1, new SentinelSpider());
        Permanent blocker = addCreatureReady(player2, new SentinelSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void tappedSpiderCannotBlockFlyingCreature() {
        addCreatureReady(player1, new SerraAngel());
        Permanent spider = addCreatureReady(player2, new SentinelSpider());
        spider.tap();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(spider.isBlocking()).isFalse();
    }
}
