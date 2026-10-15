package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvenFisher;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpireTracer.class, AvenFisher.class, GiantSpider.class, GrizzlyBears.class})
class SpireTracerTest extends BaseCardTest {

    @Test
    @DisplayName("Spire Tracer cannot be blocked by a creature without flying or reach")
    void cannotBeBlockedByNormalCreature() {
        attackingTracer();

        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by creatures with flying or reach");
    }

    @Test
    @DisplayName("Spire Tracer can be blocked by a creature with flying")
    void canBeBlockedByFlyingCreature() {
        attackingTracer();

        Permanent flyer = addCreatureReady(player2, new AvenFisher());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(flyer.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Spire Tracer can be blocked by a creature with reach")
    void canBeBlockedByReachCreature() {
        attackingTracer();

        Permanent spider = addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(spider.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Spire Tracer can block a creature without flying or reach")
    void canBlockNormalCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);
        Permanent tracer = addCreatureReady(player2, new SpireTracer());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(tracer.isBlocking()).isTrue();
    }

    private void attackingTracer() {
        Permanent tracer = addCreatureReady(player1, new SpireTracer());
        tracer.setAttacking(true);
    }
}
