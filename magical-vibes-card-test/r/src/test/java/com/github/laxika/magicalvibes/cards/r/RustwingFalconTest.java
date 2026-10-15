package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RustwingFalcon.class, GreenwoodSentinel.class, GiantSpider.class})
class RustwingFalconTest extends BaseCardTest {

    @Test
    void groundCreatureCannotBlockFalcon() {
        addCreatureReady(player1, new RustwingFalcon());
        addCreatureReady(player2, new GreenwoodSentinel());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void flyingCreatureCanBlockFalcon() {
        addCreatureReady(player1, new RustwingFalcon());
        Permanent blocker = addCreatureReady(player2, new RustwingFalcon());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void reachCreatureCanBlockFalcon() {
        addCreatureReady(player1, new RustwingFalcon());
        Permanent blocker = addCreatureReady(player2, new GiantSpider());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void falconCanBlockGroundCreature() {
        addCreatureReady(player1, new GreenwoodSentinel());
        Permanent blocker = addCreatureReady(player2, new RustwingFalcon());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
