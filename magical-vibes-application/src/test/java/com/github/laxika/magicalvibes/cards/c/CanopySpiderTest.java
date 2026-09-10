package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CanopySpider.class, TrainedArmodon.class, WindDrake.class})
class CanopySpiderTest extends BaseCardTest {

    @Test
    @DisplayName("Canopy Spider can block a creature with flying")
    void canBlockFlyingCreature() {
        Permanent spider = addCreatureReady(player2, new CanopySpider());
        addCreatureReady(player1, new WindDrake());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(spider.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Canopy Spider can also block a non-flying creature")
    void canBlockNonFlyingCreature() {
        Permanent spider = addCreatureReady(player2, new CanopySpider());
        addCreatureReady(player1, new TrainedArmodon());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(spider.isBlocking()).isTrue();
    }
}
