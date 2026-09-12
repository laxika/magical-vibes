package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DivingGriffin;
import com.github.laxika.magicalvibes.cards.v.VintaraElephant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RibCageSpider.class, DivingGriffin.class, VintaraElephant.class})
class RibCageSpiderTest extends BaseCardTest {

    @Test
    @DisplayName("Rib Cage Spider can block a creature with flying")
    void canBlockFlyingCreature() {
        Permanent spider = addCreatureReady(player2, new RibCageSpider());
        addCreatureReady(player1, new DivingGriffin());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(spider.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Rib Cage Spider can also block a non-flying creature")
    void canBlockNonFlyingCreature() {
        Permanent spider = addCreatureReady(player2, new RibCageSpider());
        addCreatureReady(player1, new VintaraElephant());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(spider.isBlocking()).isTrue();
    }
}
