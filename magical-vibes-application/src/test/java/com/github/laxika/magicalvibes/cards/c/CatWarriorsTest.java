package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CatWarriors.class, Forest.class, GrizzlyBears.class})
class CatWarriorsTest extends BaseCardTest {

    @Test
    @DisplayName("Forestwalk: cannot be blocked if defending player controls a Forest")
    void forestwalkCannotBeBlockedWhenDefenderHasForest() {
        harness.addToBattlefield(player2, new Forest());

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        Permanent cat = addCreatureReady(player1, new CatWarriors());
        cat.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(cat);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Forestwalk: can be blocked if defending player controls no Forest")
    void forestwalkAllowsBlockingWhenDefenderHasNoForest() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        Permanent cat = addCreatureReady(player1, new CatWarriors());
        cat.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(cat);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Forestwalk: an attacking player's Forest does not prevent blocking")
    void forestwalkChecksDefendingPlayerOnly() {
        harness.addToBattlefield(player1, new Forest());

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent cat = addCreatureReady(player1, new CatWarriors());
        cat.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(cat);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
