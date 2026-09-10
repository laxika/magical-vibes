package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EliteCatWarrior.class, Forest.class, GrizzlyBears.class, Island.class})
class EliteCatWarriorTest extends BaseCardTest {

    @Test
    @DisplayName("Forestwalk: cannot be blocked if defending player controls a Forest")
    void forestwalkCannotBeBlockedWhenDefenderHasForest() {
        harness.addToBattlefield(player2, new Forest());

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        Permanent cat = addCreatureReady(player1, new EliteCatWarrior());
        cat.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, cat))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Forestwalk: can be blocked if defending player controls no Forest")
    void forestwalkAllowsBlockingWhenDefenderHasNoForest() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        Permanent cat = addCreatureReady(player1, new EliteCatWarrior());
        cat.setAttacking(true);

        prepareDeclareBlockers();

        declareBlock(blocker, cat);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Forestwalk does not apply when defending player controls another basic land type")
    void forestwalkAllowsBlockingWhenDefenderHasIsland() {
        harness.addToBattlefield(player2, new Island());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent cat = addCreatureReady(player1, new EliteCatWarrior());
        cat.setAttacking(true);

        prepareDeclareBlockers();
        declareBlock(blocker, cat);

        assertThat(blocker.isBlocking()).isTrue();
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }
}
