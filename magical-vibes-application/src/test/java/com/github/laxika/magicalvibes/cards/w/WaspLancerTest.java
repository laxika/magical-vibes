package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CrabappleCohort;
import com.github.laxika.magicalvibes.cards.g.Gloomwidow;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WaspLancer.class, CrabappleCohort.class, Gloomwidow.class})
class WaspLancerTest extends BaseCardTest {

    @Test
    void cannotBeBlockedByCreatureWithoutFlyingOrReach() {
        addCreatureReady(player1, new WaspLancer());
        Permanent blocker = addCreatureReady(player2, new CrabappleCohort());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    void canBeBlockedByCreatureWithFlying() {
        addCreatureReady(player1, new WaspLancer());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new WaspLancer());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void canBeBlockedByCreatureWithReach() {
        addCreatureReady(player1, new WaspLancer());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new Gloomwidow());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void canBlockCreatureWithoutFlyingWhileSummoningSick() {
        addCreatureReady(player1, new CrabappleCohort());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new WaspLancer());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
