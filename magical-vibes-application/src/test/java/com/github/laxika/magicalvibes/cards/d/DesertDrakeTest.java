package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GoblinBully;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DesertDrake.class, GoblinBully.class})
class DesertDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking Desert Drake")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new DesertDrake());
        addCreatureReady(player2, new GoblinBully());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("A creature with flying can block Desert Drake")
    void flyingCreatureCanBlockDesertDrake() {
        addCreatureReady(player1, new DesertDrake());
        Permanent blocker = addCreatureReady(player2, new DesertDrake());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Desert Drake can block a creature without flying")
    void canBlockNonFlyingCreature() {
        addCreatureReady(player1, new GoblinBully());
        Permanent blocker = addCreatureReady(player2, new DesertDrake());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
