package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HallsOfMistTest extends BaseCardTest {

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("A creature that attacked during its controller's last turn can't attack")
    void attackerFromLastTurnCantAttack() {
        Permanent bear = addReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        bear.setAttacking(true);
        bear.clearCombatState();

        // player1 -> player2 -> player1: back on the bear's controller's turn.
        advanceTurn();
        advanceTurn();
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        addReady(player2, new HallsOfMist());

        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId())).doesNotContain(0);
    }

    @Test
    @DisplayName("A creature that did not attack during its controller's last turn may still attack")
    void nonAttackerCanAttack() {
        addReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        advanceTurn();
        advanceTurn();
        addReady(player2, new HallsOfMist());

        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId())).contains(0);
    }

    @Test
    @DisplayName("The restriction only looks one controller turn back, so it lifts after a turn spent not attacking")
    void restrictionLiftsAfterAnIdleTurn() {
        Permanent bear = addReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        bear.setAttacking(true);
        bear.clearCombatState();

        advanceTurn();
        advanceTurn();
        Permanent halls = addReady(player2, new HallsOfMist());
        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId())).doesNotContain(0);

        // The bear sits out this turn, so on the next one it is a legal attacker again.
        gd.playerBattlefields.get(player2.getId()).remove(halls);
        advanceTurn();
        advanceTurn();
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        gd.playerBattlefields.get(player2.getId()).add(halls);
        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId())).contains(0);
    }

    @Test
    @DisplayName("Without Halls of Mist on the battlefield a creature may attack on consecutive turns")
    void noRestrictionWithoutHallsOfMist() {
        Permanent bear = addReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        bear.setAttacking(true);
        bear.clearCombatState();

        advanceTurn();
        advanceTurn();

        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId())).contains(0);
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
