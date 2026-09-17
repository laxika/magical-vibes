package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NantukoElder;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeafDancer.class, Forest.class, NantukoElder.class})
class LeafDancerTest extends BaseCardTest {

    @Test
    @DisplayName("Leaf Dancer cannot be blocked when defending player controls a Forest")
    void cannotBeBlockedWhenDefenderControlsForest() {
        harness.addToBattlefield(player2, new Forest());

        Permanent blocker = addCreatureReady(player2, new NantukoElder());

        Permanent leafDancer = addCreatureReady(player1, new LeafDancer());
        leafDancer.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(leafDancer);

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Leaf Dancer can be blocked when defending player controls no Forest")
    void canBeBlockedWhenDefenderDoesNotControlForest() {
        Permanent blocker = addCreatureReady(player2, new NantukoElder());

        Permanent leafDancer = addCreatureReady(player1, new LeafDancer());
        leafDancer.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(leafDancer);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Leaf Dancer can be blocked when only the attacking player controls a Forest")
    void canBeBlockedWhenOnlyAttackingPlayerControlsForest() {
        harness.addToBattlefield(player1, new Forest());

        Permanent blocker = addCreatureReady(player2, new NantukoElder());
        Permanent leafDancer = addCreatureReady(player1, new LeafDancer());
        leafDancer.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(leafDancer);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
