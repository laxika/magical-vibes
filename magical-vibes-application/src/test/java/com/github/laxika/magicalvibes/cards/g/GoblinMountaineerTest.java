package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

@CardUsed({GoblinMountaineer.class, HillGiant.class, Mountain.class})
class GoblinMountaineerTest extends BaseCardTest {

    @Test
    @DisplayName("Goblin Mountaineer cannot be blocked when defending player controls a Mountain")
    void cannotBeBlockedWhenDefenderControlsMountain() {
        harness.addToBattlefield(player2, new Mountain());

        Permanent blockerPerm = addCreatureReady(player2, new HillGiant());

        Permanent atkPerm = addCreatureReady(player1, new GoblinMountaineer());

        declareAttackersAndPrepareBlockers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm)));

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Goblin Mountaineer cannot be blocked even when the defending Mountain is tapped")
    void cannotBeBlockedWhenDefendingMountainIsTapped() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        mountain.tap();
        Permanent blocker = addCreatureReady(player2, new HillGiant());
        Permanent attacker = addCreatureReady(player1, new GoblinMountaineer());

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        declareAttackersAndPrepareBlockers(List.of(attackerIndex));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Goblin Mountaineer can be blocked when defending player does not control a Mountain")
    void canBeBlockedWhenDefenderDoesNotControlMountain() {
        Permanent blockerPerm = addCreatureReady(player2, new HillGiant());

        Permanent atkPerm = addCreatureReady(player1, new GoblinMountaineer());

        declareAttackersAndPrepareBlockers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm)));

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Goblin Mountaineer can be blocked when only the attacking player controls a Mountain")
    void canBeBlockedWhenOnlyAttackingPlayerControlsMountain() {
        harness.addToBattlefield(player1, new Mountain());

        Permanent blockerPerm = addCreatureReady(player2, new HillGiant());
        Permanent atkPerm = addCreatureReady(player1, new GoblinMountaineer());

        declareAttackersAndPrepareBlockers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm)));

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }
}
