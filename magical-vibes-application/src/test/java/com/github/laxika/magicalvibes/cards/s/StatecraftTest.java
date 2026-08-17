package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StatecraftTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents combat damage to and by creatures you control")
    void preventsCombatDamageToAndByYourCreatures() {
        harness.addToBattlefield(player1, new Statecraft());
        Permanent blocker = addCreature(player1);
        Permanent attacker = addAttacker(player2);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat(player2);

        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Prevents combat damage dealt by your creatures to players")
    void preventsYourCreaturesFromDealingCombatDamage() {
        harness.addToBattlefield(player1, new Statecraft());
        harness.setLife(player2, 20);
        addAttacker(player1);

        resolveCombat(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not prevent combat damage from creatures you do not control")
    void doesNotPreventOpponentsCombatDamage() {
        harness.addToBattlefield(player1, new Statecraft());
        harness.setLife(player1, 20);
        addAttacker(player2);

        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not prevent noncombat damage")
    void doesNotPreventNoncombatDamage() {
        harness.addToBattlefield(player1, new Statecraft());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ProdigalPyromancer());
        creature.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(creature), null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    private Permanent addCreature(Player owner) {
        return harness.addToBattlefieldAndReturn(owner, new GrizzlyBears());
    }

    private Permanent addAttacker(Player owner) {
        Permanent attacker = addCreature(owner);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(owner.getId().equals(player1.getId()) ? player2.getId() : player1.getId());
        return attacker;
    }
}
