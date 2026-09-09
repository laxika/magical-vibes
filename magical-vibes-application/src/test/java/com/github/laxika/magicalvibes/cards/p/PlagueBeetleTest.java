package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PlagueBeetle.class, GiantCockroach.class, Swamp.class})
class PlagueBeetleTest extends BaseCardTest {


    @Test
    @DisplayName("Plague Beetle cannot be blocked when defending player controls a Swamp")
    void cannotBeBlockedWhenDefenderControlsSwamp() {
        harness.addToBattlefield(player2, new Swamp());

        Permanent blockerPerm = addCreatureReady(player2, new GiantCockroach());

        Permanent attackerPerm = addCreatureReady(player1, new PlagueBeetle());
        attackerPerm.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attackerPerm);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Plague Beetle can be blocked when defending player does not control a Swamp")
    void canBeBlockedWhenDefenderDoesNotControlSwamp() {
        harness.setLife(player2, 20);

        Permanent blockerPerm = addCreatureReady(player2, new GiantCockroach());

        Permanent attackerPerm = addCreatureReady(player1, new PlagueBeetle());
        attackerPerm.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attackerPerm);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Unblocked Plague Beetle deals 1 damage to defending player")
    void dealsOneDamageWhenUnblocked() {
        harness.setLife(player2, 20);

        Permanent attackerPerm = addCreatureReady(player1, new PlagueBeetle());
        attackerPerm.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void attackersSwampDoesNotEnableSwampwalk() {
        Permanent blockerPerm = addCreatureReady(player2, new GiantCockroach());
        Permanent attackerPerm = addCreatureReady(player1, new PlagueBeetle());
        attackerPerm.setAttacking(true);
        harness.addToBattlefield(player1, new Swamp());

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attackerPerm);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }
}
