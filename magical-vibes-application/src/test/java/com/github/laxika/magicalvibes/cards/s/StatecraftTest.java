package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.m.MinimusContainment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.cards.t.Tremor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Statecraft.class, FreshVolunteers.class, Sizzle.class, Tremor.class})
class StatecraftTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents combat damage to and by creatures you control")
    void preventsCombatDamageToAndByYourCreatures() {
        harness.addToBattlefield(player1, new Statecraft());
        Permanent blocker = addCreatureReady(player1, new FreshVolunteers());
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
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new Sizzle(), "{2}{R}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Does not prevent noncombat damage to creatures you control")
    void doesNotPreventNoncombatDamageToYourCreatures() {
        harness.addToBattlefield(player1, new Statecraft());
        Permanent creature = addCreatureReady(player1, new FreshVolunteers());
        harness.castFromHand(player1, new Tremor(), "{R}");
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @CardUsed(MinimusContainment.class)
    @DisplayName("Stops preventing combat damage when Statecraft loses its abilities")
    void stopsPreventingCombatDamageWhenItLosesItsAbilities() {
        Permanent statecraft = harness.addToBattlefieldAndReturn(player1, new Statecraft());
        harness.setHand(player1, List.of(new MinimusContainment()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, statecraft.getId());
        harness.passBothPriorities();

        harness.setLife(player2, 20);
        addAttacker(player1);
        resolveCombat(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    private Permanent addAttacker(Player owner) {
        Permanent attacker = addCreatureReady(owner, new FreshVolunteers());
        attacker.setAttacking(true);
        attacker.setAttackTarget(owner.getId().equals(player1.getId()) ? player2.getId() : player1.getId());
        return attacker;
    }
}
