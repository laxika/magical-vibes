package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AvenReedstalker;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BitterbowSharpshooters.class, AvenReedstalker.class})
class BitterbowSharpshootersTest extends BaseCardTest {

    @Test
    void vigilanceKeepsAttackerUntapped() {
        Permanent sharpshooters = addCreatureReady(player1, new BitterbowSharpshooters());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(sharpshooters.isAttacking()).isTrue();
        assertThat(sharpshooters.isTapped()).isFalse();
    }

    @Test
    void reachAllowsBlockingFlyingCreature() {
        addCreatureReady(player1, new AvenReedstalker());
        Permanent sharpshooters = addCreatureReady(player2, new BitterbowSharpshooters());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(sharpshooters.isBlocking()).isTrue();
    }

    @Test
    void reachAllowsBlockingGroundCreature() {
        addCreatureReady(player1, new BitterbowSharpshooters());
        Permanent sharpshooters = addCreatureReady(player2, new BitterbowSharpshooters());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(sharpshooters.isBlocking()).isTrue();
    }

    @Test
    void reachDoesNotLetGroundAttackerEvadeFlyingBlocker() {
        addCreatureReady(player1, new BitterbowSharpshooters());
        Permanent blocker = addCreatureReady(player2, new AvenReedstalker());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void vigilanceDoesNotAllowTappedCreatureToAttack() {
        Permanent sharpshooters = addCreatureReady(player1, new BitterbowSharpshooters());
        sharpshooters.tap();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(sharpshooters.isAttacking()).isFalse();
    }
}
