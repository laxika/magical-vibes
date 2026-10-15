package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.Brushstrider;
import com.github.laxika.magicalvibes.cards.s.SunspireGriffin;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ToweringIndrik.class, SunspireGriffin.class, Brushstrider.class})
class ToweringIndrikTest extends BaseCardTest {

    @Test
    void canBlockFlyingCreature() {
        addCreatureReady(player1, new SunspireGriffin());
        Permanent indrik = addCreatureReady(player2, new ToweringIndrik());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(indrik.isBlocking()).isTrue();
    }

    @Test
    void canBlockCreatureWithoutFlying() {
        addCreatureReady(player1, new Brushstrider());
        Permanent indrik = addCreatureReady(player2, new ToweringIndrik());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(indrik.isBlocking()).isTrue();
    }

    @Test
    void reachDoesNotPreventGroundCreatureFromBlockingIt() {
        addCreatureReady(player1, new ToweringIndrik());
        Permanent blocker = addCreatureReady(player2, new Brushstrider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void tappedIndrikCannotBlockFlyingCreature() {
        addCreatureReady(player1, new SunspireGriffin());
        Permanent indrik = addCreatureReady(player2, new ToweringIndrik());

        declareAttackersAndPrepareBlockers(List.of(0));
        indrik.tap();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
        assertThat(indrik.isBlocking()).isFalse();
    }
}
