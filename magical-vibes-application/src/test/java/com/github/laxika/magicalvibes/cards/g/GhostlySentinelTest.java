package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LongbowArcher;
import com.github.laxika.magicalvibes.cards.w.Warthog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhostlySentinel.class, LongbowArcher.class, Warthog.class})
class GhostlySentinelTest extends BaseCardTest {

    @Test
    void flyingAndVigilanceWork() {
        Permanent sentinel = addCreatureReady(player1, new GhostlySentinel());
        addCreatureReady(player2, new Warthog());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(sentinel.isTapped()).isFalse();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void creatureWithReachCanBlock() {
        addCreatureReady(player1, new GhostlySentinel());
        Permanent archer = addCreatureReady(player2, new LongbowArcher());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(archer.isBlocking()).isTrue();
    }

    @Test
    void flyingCreatureCanBlockSentinel() {
        addCreatureReady(player1, new GhostlySentinel());
        Permanent blocker = addCreatureReady(player2, new GhostlySentinel());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void vigilanceDoesNotAllowAttackingWhileTapped() {
        Permanent sentinel = addCreatureReady(player1, new GhostlySentinel());
        sentinel.tap();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThat(sentinel.isAttacking()).isFalse();
    }
}
