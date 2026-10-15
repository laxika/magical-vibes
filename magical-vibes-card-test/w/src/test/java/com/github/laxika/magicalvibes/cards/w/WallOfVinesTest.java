package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.s.StormfrontPegasus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WallOfVines.class, StormfrontPegasus.class, RuneclawBear.class})
class WallOfVinesTest extends BaseCardTest {

    @Test
    void defenderPreventsAttackingEvenWhenNotSummoningSick() {
        Permanent wall = addCreatureReady(player1, new WallOfVines());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        assertThat(wall.isAttacking()).isFalse();
        assertThat(wall.isTapped()).isFalse();
    }

    @Test
    void reachAllowsBlockingFlyingCreatureAndPreventsPlayerDamage() {
        addCreatureReady(player1, new StormfrontPegasus());
        Permanent wall = addCreatureReady(player2, new WallOfVines());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(wall.isBlocking()).isTrue();
        resolveCombat();

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Wall of Vines");
        harness.assertOnBattlefield(player1, "Stormfront Pegasus");
    }

    @Test
    void canBlockNonFlyingCreatureWhileSummoningSick() {
        addCreatureReady(player1, new RuneclawBear());
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfVines());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(wall.isBlocking()).isTrue();
        resolveCombat();

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Wall of Vines");
        harness.assertOnBattlefield(player1, "Runeclaw Bear");
    }

    @Test
    void reachDoesNotAllowTappedWallToBlock() {
        addCreatureReady(player1, new StormfrontPegasus());
        Permanent wall = addCreatureReady(player2, new WallOfVines());

        declareAttackersAndPrepareBlockers(List.of(0));
        wall.tap();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");

        assertThat(wall.isBlocking()).isFalse();
    }
}
