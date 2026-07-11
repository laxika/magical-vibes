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

class HeavyFogTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage from an attacking creature to you is prevented")
    void preventsCombatDamageFromAttacker() {
        harness.forceActivePlayer(player1);
        addAttacker(player1, player2);
        harness.setHand(player2, List.of(new HeavyFog()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        int defenderLifeBefore = gd.getLife(player2.getId());

        harness.castInstant(player2, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();

        // Advance to combat damage: the unblocked Grizzly Bears would deal 2 to player2.
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // All damage from the attacking creature is prevented.
        assertThat(gd.getLife(player2.getId())).isEqualTo(defenderLifeBefore);
    }

    @Test
    @DisplayName("Cannot cast if not attacked this step")
    void cannotCastWhenNotAttacked() {
        harness.forceActivePlayer(player1);
        addAttacker(player1, player1);
        harness.setHand(player2, List.of(new HeavyFog()));
        harness.addMana(player2, ManaColor.GREEN, 2);
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
        harness.setHand(player2, List.of(new HeavyFog()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.castInstant(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
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
