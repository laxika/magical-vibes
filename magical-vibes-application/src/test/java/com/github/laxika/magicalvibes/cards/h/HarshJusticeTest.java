package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HarshJusticeTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creature that deals combat damage to you reflects it to its controller")
    void reflectsCombatDamageToAttackerController() {
        harness.forceActivePlayer(player1);
        addAttacker(player1, player2);
        harness.setHand(player2, List.of(new HarshJustice()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        int attackerLifeBefore = gd.getLife(player1.getId());
        int defenderLifeBefore = gd.getLife(player2.getId());

        harness.castInstant(player2, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();

        // Advance to combat damage: the unblocked Grizzly Bears deals 2 to player2.
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Reflection trigger goes on the stack — resolve it.
        resolveStack();

        // player2 took 2 combat damage; the Grizzly Bears reflected 2 back to player1.
        assertThat(gd.getLife(player2.getId())).isEqualTo(defenderLifeBefore - 2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(attackerLifeBefore - 2);
    }

    @Test
    @DisplayName("Blocked attacker deals no combat damage to you, so nothing is reflected")
    void blockedAttackerDoesNotReflect() {
        harness.forceActivePlayer(player1);
        addAttacker(player1, player2);

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.setHand(player2, List.of(new HarshJustice()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        int attackerLifeBefore = gd.getLife(player1.getId());
        int defenderLifeBefore = gd.getLife(player2.getId());

        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveStack();

        // No combat damage to a player — no reflection, no life loss for either player.
        assertThat(gd.getLife(player2.getId())).isEqualTo(defenderLifeBefore);
        assertThat(gd.getLife(player1.getId())).isEqualTo(attackerLifeBefore);
    }

    @Test
    @DisplayName("Cannot cast if not attacked this step")
    void cannotCastWhenNotAttacked() {
        harness.forceActivePlayer(player1);
        addAttacker(player1, player1);
        harness.setHand(player2, List.of(new HarshJustice()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.castInstant(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot cast outside the declare attackers step")
    void cannotCastOutsideDeclareAttackers() {
        harness.forceActivePlayer(player1);
        addAttacker(player1, player2);
        harness.setHand(player2, List.of(new HarshJustice()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.castInstant(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private void resolveStack() {
        int guard = 0;
        while (!gd.stack.isEmpty() && guard++ < 10) {
            harness.clearPriorityPassed();
            harness.passBothPriorities();
        }
    }

    private Permanent addAttacker(Player attackerController, Player defender) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        gd.playerBattlefields.get(attackerController.getId()).add(perm);
        return perm;
    }
}
