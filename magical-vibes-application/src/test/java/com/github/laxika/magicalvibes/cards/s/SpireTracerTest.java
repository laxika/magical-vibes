package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvenFisher;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpireTracerTest extends BaseCardTest {

    @Test
    @DisplayName("Spire Tracer cannot be blocked by a creature without flying or reach")
    void cannotBeBlockedByNormalCreature() {
        gd.playerBattlefields.get(player1.getId()).add(attackingTracer());

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by creatures with flying or reach");
    }

    @Test
    @DisplayName("Spire Tracer can be blocked by a creature with flying")
    void canBeBlockedByFlyingCreature() {
        gd.playerBattlefields.get(player1.getId()).add(attackingTracer());

        Permanent flyer = new Permanent(new AvenFisher());
        flyer.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(flyer);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(flyer.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Spire Tracer can be blocked by a creature with reach")
    void canBeBlockedByReachCreature() {
        gd.playerBattlefields.get(player1.getId()).add(attackingTracer());

        Permanent spider = new Permanent(new GiantSpider());
        spider.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(spider);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(spider.isBlocking()).isTrue();
    }

    private Permanent attackingTracer() {
        Permanent tracer = new Permanent(new SpireTracer());
        tracer.setSummoningSick(false);
        tracer.setAttacking(true);
        return tracer;
    }
}
