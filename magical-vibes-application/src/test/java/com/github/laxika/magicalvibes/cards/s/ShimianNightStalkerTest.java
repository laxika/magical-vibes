package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShimianNightStalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Redirects combat damage from only the targeted attacking creature")
    void redirectsOnlyTargetedAttackerCombatDamage() {
        Permanent shimian = addReady(player2, new ShimianNightStalker());
        Permanent targetedAttacker = addAttacker(player1, new GrizzlyBears(), player2);
        Permanent otherAttacker = addAttacker(player1, new GrizzlyBears(), player2);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.activateAbility(player2, indexOf(player2, shimian), null, targetedAttacker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(shimian);
    }

    @Test
    @DisplayName("Redirects noncombat damage from the targeted creature after it stops attacking")
    void redirectsNoncombatDamageFromTargetedCreature() {
        Permanent shimian = addReady(player1, new ShimianNightStalker());
        Permanent attacker = addReady(player1, new ProdigalPyromancer());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, indexOf(player1, shimian), null, attacker.getId());
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        attacker.setAttacking(false);
        harness.activateAbility(player1, indexOf(player1, attacker), null, player1.getId());
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(shimian.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Rejects a creature that is not attacking")
    void rejectsNonAttackingTarget() {
        Permanent shimian = addReady(player1, new ShimianNightStalker());
        Permanent creature = addReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, shimian), null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(shimian.isTapped()).isFalse();
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addAttacker(Player player, Card card, Player defender) {
        Permanent attacker = addReady(player, card);
        attacker.setAttacking(true);
        attacker.setAttackTarget(defender.getId());
        return attacker;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
