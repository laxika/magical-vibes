package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GoldmeadowStalwart;
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

@CardUsed({HillcomberGiant.class, GoldmeadowStalwart.class, Mountain.class})
class HillcomberGiantTest extends BaseCardTest {

    @Test
    @DisplayName("A tapped Mountain still makes Hillcomber Giant unblockable")
    void cannotBeBlockedWhenDefendersMountainIsTapped() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        mountain.tap();
        Permanent blocker = addCreatureReady(player2, new GoldmeadowStalwart());
        Permanent attacker = addCreatureReady(player1, new HillcomberGiant());
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        declareAttackersAndPrepareBlockers(List.of(attackerIndex));
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Hillcomber Giant cannot be blocked when defending player controls a Mountain")
    void cannotBeBlockedWhenDefenderControlsMountain() {
        harness.addToBattlefield(player2, new Mountain());

        Permanent blockerPerm = addCreatureReady(player2, new GoldmeadowStalwart());

        Permanent atkPerm = addCreatureReady(player1, new HillcomberGiant());
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);
        declareAttackersAndPrepareBlockers(List.of(attackerIdx));

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Hillcomber Giant can be blocked when defending player does not control a Mountain")
    void canBeBlockedWhenDefenderDoesNotControlMountain() {
        Permanent blockerPerm = addCreatureReady(player2, new GoldmeadowStalwart());

        Permanent atkPerm = addCreatureReady(player1, new HillcomberGiant());
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);
        declareAttackersAndPrepareBlockers(List.of(attackerIdx));

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Hillcomber Giant can be blocked when only the attacking player controls a Mountain")
    void mountainwalkChecksDefendingPlayerOnly() {
        harness.addToBattlefield(player1, new Mountain());

        Permanent blockerPerm = addCreatureReady(player2, new GoldmeadowStalwart());
        Permanent atkPerm = addCreatureReady(player1, new HillcomberGiant());
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);
        declareAttackersAndPrepareBlockers(List.of(attackerIdx));
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }
}
