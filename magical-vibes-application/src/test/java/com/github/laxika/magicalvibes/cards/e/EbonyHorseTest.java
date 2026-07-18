package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

class EbonyHorseTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps the target attacking creature you control")
    void untapsTargetAttacker() {
        Permanent ebonyHorse = addEbonyHorse(player1);
        Permanent attacker = addAttacker(player1, player2, 2, 2);
        attacker.tap();

        activateEbonyHorse(ebonyHorse, attacker);

        assertThat(attacker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Activating taps Ebony Horse")
    void activatingTapsEbonyHorse() {
        Permanent ebonyHorse = addEbonyHorse(player1);
        Permanent attacker = addAttacker(player1, player2, 2, 2);

        activateEbonyHorse(ebonyHorse, attacker);

        assertThat(ebonyHorse.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Prevents combat damage the target creature would deal to a player")
    void preventsCombatDamageDealtByCreature() {
        harness.setLife(player2, 20);
        Permanent ebonyHorse = addEbonyHorse(player1);
        Permanent attacker = addAttacker(player1, player2, 2, 2);

        activateEbonyHorse(ebonyHorse, attacker);
        resolveCombat();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Prevents combat damage dealt to the target creature by a blocker")
    void preventsCombatDamageDealtToCreature() {
        Permanent ebonyHorse = addEbonyHorse(player1);
        Permanent attacker = addAttacker(player1, player2, 2, 2);
        addBlocker(player2, 3, 3, 0);

        activateEbonyHorse(ebonyHorse, attacker);
        resolveCombat();

        // A 3/3 blocker would normally kill the 2/2 attacker; combat damage to it is prevented.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(attacker.getId()));
    }

    @Test
    @DisplayName("Only combat damage is prevented; noncombat damage still lands")
    void doesNotPreventNoncombatDamage() {
        Permanent ebonyHorse = addEbonyHorse(player1);
        Permanent attacker = addAttacker(player1, player2, 2, 2);

        activateEbonyHorse(ebonyHorse, attacker);

        assertThat(gd.creaturesWithCombatDamagePrevented).contains(attacker.getId());
        assertThat(gd.creaturesWithAllDamagePrevented).doesNotContain(attacker.getId());
    }

    @Test
    @DisplayName("Cannot target an attacking creature an opponent controls")
    void cannotTargetOpponentsAttacker() {
        Permanent ebonyHorse = addEbonyHorse(player1);
        Permanent opponentAttacker = addAttacker(player2, player1, 2, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(ebonyHorse);
        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, opponentAttacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonAttacker() {
        Permanent ebonyHorse = addEbonyHorse(player1);
        Permanent bystander = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(ebonyHorse);
        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, bystander.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent addEbonyHorse(Player owner) {
        Permanent perm = new Permanent(new EbonyHorse());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(owner.getId()).add(perm);
        return perm;
    }

    private void activateEbonyHorse(Permanent ebonyHorse, Permanent target) {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(ebonyHorse);
        harness.activateAbility(player1, index, null, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addAttacker(Player owner, Player defender, int power, int toughness) {
        Card bears = new GrizzlyBears();
        bears.setPower(power);
        bears.setToughness(toughness);
        Permanent perm = new Permanent(bears);
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        gd.playerBattlefields.get(owner.getId()).add(perm);
        return perm;
    }

    private Permanent addBlocker(Player owner, int power, int toughness, int blockedAttackerIndex) {
        Card bears = new GrizzlyBears();
        bears.setPower(power);
        bears.setToughness(toughness);
        Permanent perm = new Permanent(bears);
        perm.setSummoningSick(false);
        perm.setBlocking(true);
        perm.addBlockingTarget(blockedAttackerIndex);
        gd.playerBattlefields.get(owner.getId()).add(perm);
        return perm;
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
