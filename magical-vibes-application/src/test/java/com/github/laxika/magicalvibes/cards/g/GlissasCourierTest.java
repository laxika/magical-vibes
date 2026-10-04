package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlissasCourier.class, Mountain.class})
class GlissasCourierTest extends BaseCardTest {

    @Test
    void cannotBeBlockedWhenDefenderControlsMountain() {
        harness.addToBattlefield(player2, new Mountain());
        Permanent blocker = addCreatureReady(player2, new GlissasCourier());
        addCreatureReady(player1, new GlissasCourier());
        declareAttackersAndPrepareBlockers(List.of(0));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    void canBeBlockedWhenDefenderControlsNoMountain() {
        Permanent blocker = addCreatureReady(player2, new GlissasCourier());
        Permanent attacker = addCreatureReady(player1, new GlissasCourier());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.getBlockingTargetIds()).containsExactly(attacker.getId());
    }

    @Test
    void attackersMountainDoesNotPreventBlocking() {
        harness.addToBattlefield(player1, new Mountain());
        Permanent blocker = addCreatureReady(player2, new GlissasCourier());
        Permanent attacker = addCreatureReady(player1, new GlissasCourier());
        declareAttackersAndPrepareBlockers(List.of(1));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.getBlockingTargetIds()).containsExactly(attacker.getId());
    }
}
