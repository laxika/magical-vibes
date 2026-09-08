package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({IthHighArcanist.class, GrizzlyBears.class, Shock.class})
class IthHighArcanistTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps the target attacking creature")
    void untapsTargetAttacker() {
        Permanent ith = addIth();
        Permanent attacker = addAttacker(player1, player2, 2, 2);
        attacker.tap();

        activateIth(ith, attacker);

        assertThat(attacker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Prevents combat damage dealt by the target creature")
    void preventsCombatDamageDealtByTarget() {
        harness.setLife(player2, 20);
        Permanent ith = addIth();
        Permanent attacker = addAttacker(player1, player2, 2, 2);

        activateIth(ith, attacker);
        resolveCombat();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Prevents combat damage dealt to the target creature")
    void preventsCombatDamageDealtToTarget() {
        Permanent ith = addIth();
        Permanent attacker = addAttacker(player1, player2, 2, 2);
        addBlocker(player2, 3, 3, 0);

        activateIth(ith, attacker);
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
    }

    @Test
    @DisplayName("Does not prevent noncombat damage to the target creature")
    void doesNotPreventNoncombatDamage() {
        Permanent ith = addIth();
        Permanent attacker = addAttacker(player1, player2, 3, 3);

        activateIth(ith, attacker);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonAttacker() {
        Permanent ith = addIth();
        Permanent bystander = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareActivation();

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(ith);
        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, bystander.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target an attacking creature controlled by another player")
    void canTargetOpponentsAttacker() {
        Permanent ith = addIth();
        Permanent attacker = addAttacker(player2, player1, 2, 2);

        activateIth(ith, attacker);

        assertThat(attacker.isTapped()).isFalse();
    }

    private Permanent addIth() {
        Permanent ith = harness.addToBattlefieldAndReturn(player1, new IthHighArcanist());
        ith.setSummoningSick(false);
        return ith;
    }

    private void activateIth(Permanent ith, Permanent target) {
        prepareActivation();
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(ith);
        harness.activateAbility(player1, index, null, target.getId());
        harness.passBothPriorities();
    }

    private void prepareActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
    }

    private Permanent addAttacker(Player owner, Player defender, int power, int toughness) {
        Card bears = new GrizzlyBears();
        bears.setPower(power);
        bears.setToughness(toughness);
        Permanent attacker = new Permanent(bears);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(defender.getId());
        gd.playerBattlefields.get(owner.getId()).add(attacker);
        return attacker;
    }

    private Permanent addBlocker(Player owner, int power, int toughness, int blockedAttackerIndex) {
        Card bears = new GrizzlyBears();
        bears.setPower(power);
        bears.setToughness(toughness);
        Permanent blocker = new Permanent(bears);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(blockedAttackerIndex);
        gd.playerBattlefields.get(owner.getId()).add(blocker);
        return blocker;
    }
}
