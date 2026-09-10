package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrimMonolith;
import com.github.laxika.magicalvibes.cards.y.YavimayaWurm;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BouncingBeebles.class, GrimMonolith.class, YavimayaWurm.class})
class BouncingBeeblesTest extends BaseCardTest {

    @Test
    @DisplayName("Bouncing Beebles can't be blocked when defending player controls an artifact")
    void cantBeBlockedWhenDefenderControlsArtifact() {
        harness.addToBattlefield(player2, new GrimMonolith());

        Permanent blocker = addCreatureReady(player2, new YavimayaWurm());

        Permanent beebles = addCreatureReady(player1, new BouncingBeebles());
        beebles.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(beebles);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Bouncing Beebles can be blocked when defending player controls no artifacts")
    void canBeBlockedWhenDefenderControlsNoArtifact() {
        Permanent blocker = addCreatureReady(player2, new YavimayaWurm());

        Permanent beebles = addCreatureReady(player1, new BouncingBeebles());
        beebles.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void canBeBlockedWhenOnlyAttackerControlsArtifact() {
        harness.addToBattlefield(player1, new GrimMonolith());

        Permanent blocker = addCreatureReady(player2, new YavimayaWurm());

        Permanent beebles = addCreatureReady(player1, new BouncingBeebles());
        beebles.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
