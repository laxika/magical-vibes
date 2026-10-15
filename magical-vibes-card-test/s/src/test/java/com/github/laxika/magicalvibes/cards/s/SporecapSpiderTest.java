package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DawnglareInvoker;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SporecapSpider.class, DawnglareInvoker.class})
class SporecapSpiderTest extends BaseCardTest {

    @Test
    @DisplayName("Sporecap Spider can block a creature with flying")
    void canBlockFlyingCreature() {
        addCreatureReady(player1, new DawnglareInvoker());
        Permanent spider = addCreatureReady(player2, new SporecapSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(spider.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Reach allows blocking a creature without flying")
    void canBlockNonFlyingCreature() {
        addCreatureReady(player1, new SporecapSpider());
        Permanent blocker = addCreatureReady(player2, new SporecapSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A tapped Sporecap Spider cannot block a flying creature")
    void tappedSpiderCannotBlockFlyingCreature() {
        addCreatureReady(player1, new DawnglareInvoker());
        Permanent spider = addCreatureReady(player2, new SporecapSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        spider.tap();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
        assertThat(spider.isBlocking()).isFalse();
    }
}
