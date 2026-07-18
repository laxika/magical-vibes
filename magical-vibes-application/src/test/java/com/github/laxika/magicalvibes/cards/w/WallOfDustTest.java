package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WallOfDustTest extends BaseCardTest {

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("When Wall of Dust blocks an attacker, the block trigger references it and flags it as can't-attack-next-turn")
    void blockingFlagsAttacker() {
        Permanent attacker = addReadyBear(player1);
        attacker.setAttacking(true);
        addReadyWall(player2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // The block trigger references the blocked attacker (not Wall of Dust itself)
        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Wall of Dust")
                        && se.getTargetId().equals(attacker.getId()));

        harness.passBothPriorities();
        assertThat(attacker.isCantAttackNextTurn()).isTrue();
    }

    @Test
    @DisplayName("The restriction arms only on the creature's controller's next turn, not the intervening opponent turn")
    void restrictionArmsOnControllersNextTurn() {
        Permanent bear = addReadyBear(player1);
        bear.setCantAttackNextTurn(true); // state produced by the block trigger

        // The intervening opponent turn must not arm the restriction.
        harness.forceActivePlayer(player1);
        advanceTurn();
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(bear.isCantAttackThisTurn()).isFalse();
        assertThat(bear.isCantAttackNextTurn()).isTrue();

        // The controller's next turn arms it and it can't be declared as an attacker.
        advanceTurn();
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(bear.isCantAttackThisTurn()).isTrue();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("The restriction wears off after the one turn and the creature can attack again")
    void restrictionWearsOff() {
        Permanent bear = addReadyBear(player1);
        bear.setCantAttackThisTurn(true); // already armed for player1's current turn
        harness.forceActivePlayer(player1);

        // player1 -> player2 -> player1: the following controller turn clears the restriction.
        advanceTurn();
        advanceTurn();
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(bear.isCantAttackThisTurn()).isFalse();

        // The restriction is gone, so the creature is a legal attacker again.
        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId())).contains(0);
    }

    // ===== Helpers =====

    private Permanent addReadyBear(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyWall(Player player) {
        Permanent perm = new Permanent(new WallOfDust());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
