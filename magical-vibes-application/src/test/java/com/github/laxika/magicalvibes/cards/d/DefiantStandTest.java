package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
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

class DefiantStandTest extends BaseCardTest {

    @Test
    @DisplayName("Cast during declare attackers while attacked: target gets +1/+3 and untaps")
    void boostsAndUntapsWhenAttacked() {
        harness.forceActivePlayer(player1);
        addAttackerTargeting(player1, player2);
        Permanent blocker = tappedCreature(player2);
        harness.setHand(player2, List.of(new DefiantStand()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castInstant(player2, 0, blocker.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(blocker.getPowerModifier()).isEqualTo(1);
        assertThat(blocker.getToughnessModifier()).isEqualTo(3);
        assertThat(blocker.getEffectivePower()).isEqualTo(3);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(5);
        assertThat(blocker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot cast during declare attackers if not attacked")
    void cannotCastWhenNotAttacked() {
        harness.forceActivePlayer(player1);
        // Attacker aims at nobody the caster controls.
        addAttackerTargeting(player1, player1);
        Permanent target = tappedCreature(player2);
        harness.setHand(player2, List.of(new DefiantStand()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot cast outside the declare attackers step")
    void cannotCastOutsideDeclareAttackers() {
        harness.forceActivePlayer(player1);
        addAttackerTargeting(player1, player2);
        Permanent target = tappedCreature(player2);
        harness.setHand(player2, List.of(new DefiantStand()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private Permanent addAttackerTargeting(Player attackerController, Player defender) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        gd.playerBattlefields.get(attackerController.getId()).add(perm);
        return perm;
    }

    private Permanent tappedCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        perm.tap();
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
