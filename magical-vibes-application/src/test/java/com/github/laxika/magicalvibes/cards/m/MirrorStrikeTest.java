package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MirrorStrikeTest extends BaseCardTest {

    @Test
    @DisplayName("Redirects combat damage from the target attacker to its controller")
    void redirectsTargetCombatDamageToItsController() {
        Permanent target = addAttacker(player2, player1, new CrawWurm());
        int protectedLifeBefore = gd.getLife(player1.getId());
        int attackerControllerLifeBefore = gd.getLife(player2.getId());
        castMirrorStrike(target);

        assertThat(gd.getLife(player1.getId())).isEqualTo(protectedLifeBefore);
        assertThat(gd.getLife(player2.getId())).isEqualTo(attackerControllerLifeBefore - 6);
    }

    @Test
    @DisplayName("Does not redirect noncombat damage from the target attacker")
    void doesNotRedirectNoncombatDamage() {
        Permanent target = addAttacker(player2, player1, new ProdigalPyromancer());
        castMirrorStrike(target);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player2, battlefieldIndex(player2, target), null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Cannot target a blocked attacker")
    void cannotTargetBlockedAttacker() {
        Permanent target = addAttacker(player2, player1, new GrizzlyBears());
        Permanent blocker = addReadyCreature(player1, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.getBlockingTargetIds().add(target.getId());

        harness.setHand(player1, List.of(new MirrorStrike()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castMirrorStrike(Permanent target) {
        harness.setHand(player1, List.of(new MirrorStrike()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addAttacker(Player controller, Player defender, Card card) {
        Permanent attacker = addReadyCreature(controller, card);
        attacker.setAttacking(true);
        attacker.setAttackTarget(defender.getId());
        return attacker;
    }

    private Permanent addReadyCreature(Player controller, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(controller.getId()).add(permanent);
        return permanent;
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
