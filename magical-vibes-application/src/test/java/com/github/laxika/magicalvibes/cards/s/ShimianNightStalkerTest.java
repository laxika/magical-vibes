package com.github.laxika.magicalvibes.cards.s;

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

class ShimianNightStalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Redirects combat damage from only the targeted attacker")
    void redirectsCombatDamageFromTargetAttacker() {
        Permanent stalker = addReadyPermanent(player1, new ShimianNightStalker());
        Permanent targetedAttacker = addAttacker(player2, new CrawWurm());
        Permanent otherAttacker = addAttacker(player2);

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, indexOf(player1, stalker), null, targetedAttacker.getId());
        harness.passBothPriorities();

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(stalker);
    }

    @Test
    @DisplayName("Redirects noncombat damage from the targeted attacking creature")
    void redirectsNoncombatDamageFromTargetAttacker() {
        Permanent stalker = addReadyPermanent(player2, new ShimianNightStalker());
        Permanent attacker = addReadyPermanent(player2, new ProdigalPyromancer());
        attacker.setAttacking(true);

        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player2, indexOf(player2, stalker), null, attacker.getId());
        harness.passBothPriorities();

        harness.activateAbility(player2, indexOf(player2, attacker), null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(stalker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonattackingCreature() {
        Permanent stalker = addReadyPermanent(player1, new ShimianNightStalker());
        Permanent target = addReadyPermanent(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, stalker), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addAttacker(Player player) {
        return addAttacker(player, new GrizzlyBears());
    }

    private Permanent addAttacker(Player player, Card card) {
        Permanent attacker = addReadyPermanent(player, card);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
