package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.w.WildGriffin;
import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NorwoodArchers.class, WildGriffin.class, BearCub.class})
class NorwoodArchersTest extends BaseCardTest {

    @Test
    void reachAllowsBlockingFlyingCreature() {
        addCreatureReady(player1, new WildGriffin());
        Permanent archers = addCreatureReady(player2, new NorwoodArchers());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(archers.isBlocking()).isTrue();
    }

    @Test
    void reachDoesNotRestrictBlockingNonFlyingCreature() {
        addCreatureReady(player1, new BearCub());
        Permanent archers = addCreatureReady(player2, new NorwoodArchers());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(archers.isBlocking()).isTrue();
    }

    @Test
    void reachDoesNotPreventNonFlyingCreatureFromBlockingArchers() {
        addCreatureReady(player1, new NorwoodArchers());
        Permanent bears = addCreatureReady(player2, new BearCub());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(bears.isBlocking()).isTrue();
    }

    @Test
    void tappedArchersCannotBlockFlyingCreature() {
        addCreatureReady(player1, new WildGriffin());
        Permanent archers = addCreatureReady(player2, new NorwoodArchers());

        declareAttackersAndPrepareBlockers(List.of(0));
        archers.tap();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
        assertThat(archers.isBlocking()).isFalse();
    }
}

