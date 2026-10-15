package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AcademyDrake;
import com.github.laxika.magicalvibes.cards.p.PrimordialWurm;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MammothSpider.class, AcademyDrake.class, PrimordialWurm.class})
class MammothSpiderTest extends BaseCardTest {

    @Test
    void reachAllowsBlockingFlyingCreature() {
        addCreatureReady(player1, new AcademyDrake());
        Permanent spider = addCreatureReady(player2, new MammothSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(spider.isBlocking()).isTrue();
    }

    @Test
    void reachAllowsBlockingNonFlyingCreature() {
        addCreatureReady(player1, new PrimordialWurm());
        Permanent spider = addCreatureReady(player2, new MammothSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(spider.isBlocking()).isTrue();
    }

    @Test
    void nonFlyingCreatureCanBlockSpider() {
        addCreatureReady(player1, new MammothSpider());
        Permanent wurm = addCreatureReady(player2, new PrimordialWurm());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(wurm.isBlocking()).isTrue();
    }

    @Test
    void tappedSpiderCannotBlockFlyingCreature() {
        addCreatureReady(player1, new AcademyDrake());
        Permanent spider = addCreatureReady(player2, new MammothSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        spider.tap();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
        assertThat(spider.isBlocking()).isFalse();
    }
}
