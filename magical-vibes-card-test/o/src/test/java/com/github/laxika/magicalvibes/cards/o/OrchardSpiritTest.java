package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.ChapelGeist;
import com.github.laxika.magicalvibes.cards.d.DarkthicketWolf;
import com.github.laxika.magicalvibes.cards.s.SomberwaldSpider;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrchardSpirit.class, ChapelGeist.class, DarkthicketWolf.class, SomberwaldSpider.class})
class OrchardSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Orchard Spirit cannot be blocked by a creature without flying or reach")
    void cannotBeBlockedByNormalCreature() {
        attackingSpirit();

        addCreatureReady(player2, new DarkthicketWolf());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by creatures with flying or reach");
    }

    @Test
    @DisplayName("Orchard Spirit can be blocked by a creature with flying")
    void canBeBlockedByFlyingCreature() {
        attackingSpirit();

        Permanent flyer = addCreatureReady(player2, new ChapelGeist());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(flyer.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Orchard Spirit can be blocked by a creature with reach")
    void canBeBlockedByReachCreature() {
        attackingSpirit();

        Permanent spider = addCreatureReady(player2, new SomberwaldSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(spider.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Orchard Spirit can block a creature without flying or reach")
    void canBlockNormalCreature() {
        Permanent wolf = addCreatureReady(player1, new DarkthicketWolf());
        wolf.setAttacking(true);
        Permanent spirit = addCreatureReady(player2, new OrchardSpirit());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(spirit.isBlocking()).isTrue();
    }

    private void attackingSpirit() {
        Permanent spirit = addCreatureReady(player1, new OrchardSpirit());
        spirit.setAttacking(true);
    }
}
