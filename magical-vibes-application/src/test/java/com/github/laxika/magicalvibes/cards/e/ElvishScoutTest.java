package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.Aeolipile;
import com.github.laxika.magicalvibes.cards.t.Thallid;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElvishScout.class, Thallid.class, Aeolipile.class})
class ElvishScoutTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps the target attacking creature you control")
    void untapsTargetAttacker() {
        Permanent scout = addElvishScout(player1);
        Permanent attacker = addAttacker(player1, player2, 2, 2);
        attacker.tap();

        activateElvishScout(scout, attacker);

        assertThat(attacker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Activating taps Elvish Scout")
    void activatingTapsElvishScout() {
        Permanent scout = addElvishScout(player1);
        Permanent attacker = addAttacker(player1, player2, 2, 2);

        activateElvishScout(scout, attacker);

        assertThat(scout.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Prevents combat damage the target creature would deal to a player")
    void preventsCombatDamageDealtByCreature() {
        harness.setLife(player2, 20);
        Permanent scout = addElvishScout(player1);
        Permanent attacker = addAttacker(player1, player2, 2, 2);

        activateElvishScout(scout, attacker);
        resolveCombat();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Prevents combat damage dealt to the target creature by a blocker")
    void preventsCombatDamageDealtToCreature() {
        Permanent scout = addElvishScout(player1);
        Permanent attacker = addAttacker(player1, player2, 2, 2);
        addBlocker(player2, 3, 3, 0);

        activateElvishScout(scout, attacker);
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(attacker.getId()));
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Prevents combat damage but not noncombat damage")
    void doesNotPreventNoncombatDamage() {
        Permanent scout = addElvishScout(player1);
        Permanent attacker = addAttacker(player1, player2, 3, 3);

        activateElvishScout(scout, attacker);

        Permanent aeolipile = harness.addToBattlefieldAndReturn(player2, new Aeolipile());
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        int aeolipileIndex = gd.playerBattlefields.get(player2.getId()).indexOf(aeolipile);
        harness.activateAbility(player2, aeolipileIndex, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target an attacking creature an opponent controls")
    void cannotTargetOpponentsAttacker() {
        Permanent scout = addElvishScout(player1);
        Permanent opponentAttacker = addAttacker(player2, player1, 2, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(scout);
        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, opponentAttacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonAttacker() {
        Permanent scout = addElvishScout(player1);
        Permanent bystander = harness.addToBattlefieldAndReturn(player1, new Thallid());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(scout);
        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, bystander.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addElvishScout(Player owner) {
        return addCreatureReady(owner, new ElvishScout());
    }

    private void activateElvishScout(Permanent scout, Permanent target) {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(scout);
        harness.activateAbility(player1, index, null, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addAttacker(Player owner, Player defender, int power, int toughness) {
        Permanent perm = addCreatureReady(owner, new Thallid());
        perm.setPowerModifier(power - 1);
        perm.setToughnessModifier(toughness - 1);
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        return perm;
    }

    private Permanent addBlocker(Player owner, int power, int toughness, int blockedAttackerIndex) {
        Permanent perm = addCreatureReady(owner, new Thallid());
        perm.setPowerModifier(power - 1);
        perm.setToughnessModifier(toughness - 1);
        perm.setBlocking(true);
        perm.addBlockingTarget(blockedAttackerIndex);
        return perm;
    }
}
