package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmeraldOryx.class, Forest.class, RuneclawBear.class, Swamp.class})
class EmeraldOryxTest extends BaseCardTest {

    @Test
    void cannotBeBlockedWhenDefenderControlsForest() {
        harness.addToBattlefield(player2, new Forest());
        assertBlockRejected();
    }

    @Test
    void tappedForestStillPreventsBlocking() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        forest.tap();
        assertBlockRejected();
    }

    @Test
    void canBeBlockedWithoutForest() {
        assertBlockAllowed();
    }

    @Test
    void canBeBlockedWithOnlyNonForestLand() {
        harness.addToBattlefield(player2, new Swamp());
        assertBlockAllowed();
    }

    @Test
    void attackersForestDoesNotPreventBlocking() {
        harness.addToBattlefield(player1, new Forest());
        assertBlockAllowed();
    }

    private void assertBlockRejected() {
        Permanent blocker = addCreatureReady(player2, new RuneclawBear());
        Permanent attacker = addCreatureReady(player1, new EmeraldOryx());
        declareAttackersAndPrepareBlockers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        BlockerAssignment assignment = new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(assignment)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
        assertThat(blocker.isBlocking()).isFalse();
    }

    private void assertBlockAllowed() {
        Permanent blocker = addCreatureReady(player2, new RuneclawBear());
        Permanent attacker = addCreatureReady(player1, new EmeraldOryx());
        declareAttackersAndPrepareBlockers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
