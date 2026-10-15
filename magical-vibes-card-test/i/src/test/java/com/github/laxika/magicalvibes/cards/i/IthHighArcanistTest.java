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
        addBlocker(player2, 3, 3, gd.playerBattlefields.get(player1.getId()).indexOf(attacker));

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
        attacker.tap();

        activateIth(ith, attacker);

        assertThat(attacker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Suspend exiles Ith with four time counters for white and blue mana")
    void suspendsFromHand() {
        IthHighArcanist card = new IthHighArcanist();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Untapping the target leaves it attacking and does not protect other attackers")
    void otherAttackersStillDealCombatDamage() {
        Permanent ith = addIth();
        Permanent protectedAttacker = addAttacker(player1, player2, 2, 2);
        addAttacker(player1, player2, 2, 2);
        protectedAttacker.tap();
        harness.setLife(player2, 20);

        activateIth(ith, protectedAttacker);

        assertThat(protectedAttacker.isAttacking()).isTrue();
        resolveCombat();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The ability does not untap a target that stops attacking before resolution")
    void targetMustStillBeAttackingOnResolution() {
        Permanent ith = addIth();
        Permanent attacker = addAttacker(player1, player2, 2, 2);
        attacker.tap();
        prepareActivation();
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(ith);
        harness.activateAbility(player1, index, null, attacker.getId());
        attacker.setAttacking(false);

        harness.passBothPriorities();

        assertThat(attacker.isTapped()).isTrue();
    }

    private Permanent addIth() {
        return addCreatureReady(player1, new IthHighArcanist());
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
        Permanent attacker = addCreatureReady(owner, bears);
        attacker.setAttacking(true);
        attacker.setAttackTarget(defender.getId());
        return attacker;
    }

    private Permanent addBlocker(Player owner, int power, int toughness, int blockedAttackerIndex) {
        Card bears = new GrizzlyBears();
        bears.setPower(power);
        bears.setToughness(toughness);
        Permanent blocker = addCreatureReady(owner, bears);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(blockedAttackerIndex);
        return blocker;
    }
}
