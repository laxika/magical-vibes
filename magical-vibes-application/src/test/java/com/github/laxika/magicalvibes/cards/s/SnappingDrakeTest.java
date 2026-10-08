package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.g.GoliathSpider;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SnappingDrake.class, BorosRecruit.class, GoliathSpider.class})
class SnappingDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking Snapping Drake")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new SnappingDrake());
        addCreatureReady(player2, new BorosRecruit());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("A creature with flying can block Snapping Drake")
    void flyingCreatureCanBlockSnappingDrake() {
        addCreatureReady(player1, new SnappingDrake());
        Permanent blocker = addCreatureReady(player2, new SnappingDrake());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A creature with reach can block Snapping Drake")
    void reachCreatureCanBlockSnappingDrake() {
        addCreatureReady(player1, new SnappingDrake());
        Permanent blocker = addCreatureReady(player2, new GoliathSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Snapping Drake can block a creature without flying")
    void flyingCreatureCanBlockGroundAttacker() {
        addCreatureReady(player1, new BorosRecruit());
        Permanent blocker = addCreatureReady(player2, new SnappingDrake());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
