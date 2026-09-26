package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CatWarriors;
import com.github.laxika.magicalvibes.cards.c.CrawGiant;
import com.github.laxika.magicalvibes.cards.p.PsionicEntity;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShimianNightStalker.class, CrawGiant.class, CatWarriors.class, PsionicEntity.class})
class ShimianNightStalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Redirects combat damage from only the targeted attacker")
    void redirectsCombatDamageFromTargetAttacker() {
        Permanent stalker = addCreatureReady(player1, new ShimianNightStalker());
        Permanent targetedAttacker = addAttacker(player2, new CrawGiant());
        addAttacker(player2, new CatWarriors());

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
        Permanent stalker = addCreatureReady(player2, new ShimianNightStalker());
        Permanent attacker = addCreatureReady(player2, new PsionicEntity());
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
        assertThat(stalker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not redirect damage to a permanent controlled by the ability controller")
    void doesNotRedirectDamageToControllerPermanent() {
        Permanent stalker = addCreatureReady(player1, new ShimianNightStalker());
        Permanent protectedCreature = addCreatureReady(player1, new CrawGiant());
        Permanent attacker = addAttacker(player2, new PsionicEntity());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, indexOf(player1, stalker), null, attacker.getId());
        harness.passBothPriorities();

        harness.activateAbility(player2, indexOf(player2, attacker), null, protectedCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(stalker.getMarkedDamage()).isZero();
        assertThat(protectedCreature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not resolve when the targeted creature is no longer attacking")
    void targetMustStillBeAttackingWhenAbilityResolves() {
        Permanent stalker = addCreatureReady(player1, new ShimianNightStalker());
        Permanent attacker = addAttacker(player2, new CrawGiant());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, indexOf(player1, stalker), null, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        attacker.setAttacking(true);
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of());

        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
        assertThat(stalker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonattackingCreature() {
        Permanent stalker = addCreatureReady(player1, new ShimianNightStalker());
        Permanent target = addCreatureReady(player2, new CatWarriors());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, stalker), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addAttacker(Player player, Card card) {
        Permanent attacker = addCreatureReady(player, card);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
