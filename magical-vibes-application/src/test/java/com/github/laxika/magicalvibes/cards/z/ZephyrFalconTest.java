package com.github.laxika.magicalvibes.cards.z;

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

@CardUsed({ZephyrFalcon.class, GrizzlyBears.class})
class ZephyrFalconTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a nonflying creature from blocking Zephyr Falcon")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new ZephyrFalcon());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }

    @Test
    @DisplayName("A creature with flying can block Zephyr Falcon")
    void flyingCreatureCanBlockZephyrFalcon() {
        addCreatureReady(player1, new ZephyrFalcon());
        Permanent blocker = addCreatureReady(player2, new ZephyrFalcon());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Vigilance keeps Zephyr Falcon untapped when it attacks")
    void vigilanceKeepsItUntappedWhenItAttacks() {
        Permanent falcon = addCreatureReady(player1, new ZephyrFalcon());

        declareAttackers(List.of(0));

        assertThat(falcon.isTapped()).isFalse();
    }
}
