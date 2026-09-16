package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DwarvenGrunt.class, Mountain.class})
class DwarvenGruntTest extends BaseCardTest {

    @Test
    @DisplayName("Dwarven Grunt cannot be blocked when defending player controls a Mountain")
    void cannotBeBlockedWhenDefenderControlsMountain() {
        Permanent blockerPerm = addCreatureReady(player2, new DwarvenGrunt());
        harness.addToBattlefield(player2, new Mountain());
        Permanent atkPerm = addCreatureReady(player1, new DwarvenGrunt());

        declareAttackersAndPrepareBlockers(List.of(0));

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Dwarven Grunt can be blocked when defending player does not control a Mountain")
    void canBeBlockedWhenDefenderDoesNotControlMountain() {
        Permanent blockerPerm = addCreatureReady(player2, new DwarvenGrunt());
        Permanent atkPerm = addCreatureReady(player1, new DwarvenGrunt());

        declareAttackersAndPrepareBlockers(List.of(0));

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Dwarven Grunt can be blocked when only the attacking player controls a Mountain")
    void mountainwalkChecksDefendingPlayer() {
        Permanent blockerPerm = addCreatureReady(player2, new DwarvenGrunt());
        Permanent atkPerm = addCreatureReady(player1, new DwarvenGrunt());
        harness.addToBattlefield(player1, new Mountain());

        declareAttackersAndPrepareBlockers(List.of(0));

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }
}
