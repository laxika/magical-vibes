package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BorderlandRanger;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GriffinSentinel.class, BorderlandRanger.class, WindDrake.class, GiantSpider.class})
class GriffinSentinelTest extends BaseCardTest {

    @Test
    void attackingDoesNotTapSentinel() {
        Permanent sentinel = addCreatureReady(player1, new GriffinSentinel());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(sentinel.isAttacking()).isTrue();
        assertThat(sentinel.isTapped()).isFalse();
    }

    @Test
    void groundCreatureCannotBlockSentinel() {
        addCreatureReady(player1, new GriffinSentinel());
        addCreatureReady(player2, new BorderlandRanger());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void flyingCreatureCanBlockSentinel() {
        addCreatureReady(player1, new GriffinSentinel());
        Permanent blocker = addCreatureReady(player2, new WindDrake());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void reachCreatureCanBlockSentinel() {
        addCreatureReady(player1, new GriffinSentinel());
        Permanent blocker = addCreatureReady(player2, new GiantSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void sentinelCanBlockAnotherFlyingCreature() {
        addCreatureReady(player1, new WindDrake());
        Permanent sentinel = addCreatureReady(player2, new GriffinSentinel());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(sentinel.isBlocking()).isTrue();
    }
}
