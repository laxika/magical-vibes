package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JukaiMessenger.class, Forest.class, Island.class})
class JukaiMessengerTest extends BaseCardTest {

    @Test
    void cannotBeBlockedWhenDefenderControlsForest() {
        harness.addToBattlefield(player2, new Forest());
        Permanent blocker = addCreatureReady(player2, new JukaiMessenger());
        Permanent attacker = addCreatureReady(player1, new JukaiMessenger());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    void canBeBlockedWhenDefenderControlsNoForest() {
        Permanent blocker = addCreatureReady(player2, new JukaiMessenger());
        Permanent attacker = addCreatureReady(player1, new JukaiMessenger());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void canBeBlockedWhenDefenderControlsIsland() {
        harness.addToBattlefield(player2, new Island());
        Permanent blocker = addCreatureReady(player2, new JukaiMessenger());
        Permanent attacker = addCreatureReady(player1, new JukaiMessenger());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void canBeBlockedWhenOnlyAttackerControlsForest() {
        harness.addToBattlefield(player1, new Forest());
        Permanent blocker = addCreatureReady(player2, new JukaiMessenger());
        Permanent attacker = addCreatureReady(player1, new JukaiMessenger());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }
}
