package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BayouDragonfly.class, TrainedArmodon.class, Swamp.class})
class BayouDragonflyTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking Bayou Dragonfly")
    void flyingPreventsNonFlyingBlocker() {
        Permanent blocker = addCreatureReady(player2, new TrainedArmodon());
        Permanent attacker = addBayouDragonflyAttacker();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Swampwalk prevents a flying creature from blocking when the defender controls a Swamp")
    void swampwalkPreventsBlockerWhenDefenderControlsSwamp() {
        harness.addToBattlefield(player2, new Swamp());
        Permanent blocker = addCreatureReady(player2, new BayouDragonfly());
        Permanent attacker = addBayouDragonflyAttacker();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("swampwalk");
    }

    @Test
    @DisplayName("Swampwalk allows a flying creature to block when the defender controls no Swamp")
    void swampwalkAllowsBlockerWithoutSwamp() {
        Permanent blocker = addCreatureReady(player2, new BayouDragonfly());
        Permanent attacker = addBayouDragonflyAttacker();

        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addBayouDragonflyAttacker() {
        Permanent attacker = addCreatureReady(player1, new BayouDragonfly());
        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        return attacker;
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }
}
