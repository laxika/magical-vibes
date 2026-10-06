package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkysnareSpider.class, ScrapskinDrake.class})
class SkysnareSpiderTest extends BaseCardTest {

    @Test
    void vigilanceKeepsAttackingSpiderUntapped() {
        Permanent spider = addCreatureReady(player1, new SkysnareSpider());
        addCreatureReady(player2, new SkysnareSpider());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(spider.isAttacking()).isTrue();
        assertThat(spider.isTapped()).isFalse();
    }

    @Test
    void vigilanceDoesNotAllowTappedSpiderToAttack() {
        Permanent spider = addCreatureReady(player1, new SkysnareSpider());
        spider.tap();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(spider.isAttacking()).isFalse();
        assertThat(spider.isTapped()).isTrue();
    }

    @Test
    void vigilanceDoesNotAllowSummoningSickSpiderToAttack() {
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new SkysnareSpider());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(spider.isAttacking()).isFalse();
    }

    @Test
    void reachAllowsBlockingFlyingCreature() {
        addCreatureReady(player1, new ScrapskinDrake());
        Permanent spider = addCreatureReady(player2, new SkysnareSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(spider.isBlocking()).isTrue();
    }

    @Test
    void reachAllowsGroundCreaturesToBlockEachOther() {
        addCreatureReady(player1, new SkysnareSpider());
        Permanent spider = addCreatureReady(player2, new SkysnareSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(spider.isBlocking()).isTrue();
    }

    @Test
    void tappedSpiderCannotBlockFlyingCreature() {
        addCreatureReady(player1, new ScrapskinDrake());
        Permanent spider = addCreatureReady(player2, new SkysnareSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        spider.tap();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(spider.isBlocking()).isFalse();
    }

    @Test
    void summoningSickSpiderCanBlockFlyingCreature() {
        addCreatureReady(player1, new ScrapskinDrake());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new SkysnareSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(spider.isBlocking()).isTrue();
    }
}
