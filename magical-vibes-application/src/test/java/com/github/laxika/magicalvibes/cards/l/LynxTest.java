package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Lynx.class, Forest.class, BearCub.class})
class LynxTest extends BaseCardTest {

    // ===== Forestwalk =====

    @Test
    @DisplayName("Lynx cannot be blocked when defending player controls a Forest")
    void cannotBeBlockedWhenDefenderControlsForest() {
        harness.addToBattlefield(player2, new Forest());

        Permanent blockerPerm = addCreatureReady(player2, new BearCub());

        Permanent atkPerm = addCreatureReady(player1, new Lynx());
        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm)));

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Lynx can be blocked when defending player does not control a Forest")
    void canBeBlockedWhenDefenderDoesNotControlForest() {
        Permanent blockerPerm = addCreatureReady(player2, new BearCub());

        Permanent atkPerm = addCreatureReady(player1, new Lynx());
        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm)));

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Lynx can be blocked when only the attacking player controls a Forest")
    void canBeBlockedWhenOnlyAttackingPlayerControlsForest() {
        harness.addToBattlefield(player1, new Forest());
        Permanent blockerPerm = addCreatureReady(player2, new BearCub());
        Permanent atkPerm = addCreatureReady(player1, new Lynx());

        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm)));

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }
}
