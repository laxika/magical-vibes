package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JeskaThriceReborn.class, GrizzlyBears.class, ColossalDreadmaw.class})
class JeskaThriceRebornTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one loyalty counter for each commander cast from the command zone")
    void entersWithCommanderCastCounters() {
        gd.recordCommanderCastFromCommandZone(player1.getId());
        gd.recordCommanderCastFromCommandZone(player1.getId());

        Permanent jeska = harness.enterBattlefieldAndReturn(player1, new JeskaThriceReborn());

        assertThat(jeska.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Zero ability triples the targeted creature's combat damage to an opponent")
    void triplesTargetedCreatureCombatDamage() {
        gd.recordCommanderCastFromCommandZone(player1.getId());
        Permanent jeska = harness.enterBattlefieldAndReturn(player1, new JeskaThriceReborn());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(jeska), 0,
                null, bear.getId());
        harness.passBothPriorities();

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(bear)));
        resolveCombat();

        harness.assertLife(player2, 14);
        assertThat(jeska.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("Minus X ability deals X damage to up to three targets")
    void dealsDamageToThreeTargets() {
        gd.recordCommanderCastFromCommandZone(player1.getId());
        Permanent jeska = harness.enterBattlefieldAndReturn(player1, new JeskaThriceReborn());
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        Permanent third = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbilityWithMultiTargets(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(jeska), 1, 1,
                List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        assertThat(first.getMarkedDamage()).isEqualTo(1);
        assertThat(second.getMarkedDamage()).isEqualTo(1);
        assertThat(third.getMarkedDamage()).isEqualTo(1);
        assertThat(jeska.getCounterCount(CounterType.LOYALTY)).isEqualTo(0);
    }

    @Test
    void opponentCommanderCastsDoNotAddLoyalty() {
        gd.recordCommanderCastFromCommandZone(player1.getId());
        gd.recordCommanderCastFromCommandZone(player2.getId());
        gd.recordCommanderCastFromCommandZone(player2.getId());

        Permanent jeska = harness.enterBattlefieldAndReturn(player1, new JeskaThriceReborn());

        assertThat(jeska.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    void diesWithoutAnyCommanderCasts() {
        harness.enterBattlefieldAndReturn(player1, new JeskaThriceReborn());
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Jeska, Thrice Reborn");
        harness.assertInGraveyard(player1, "Jeska, Thrice Reborn");
    }

    @Test
    void dealsFullDamageToPlayersAndCreatureAfterSpendingAllLoyalty() {
        gd.recordCommanderCastFromCommandZone(player1.getId());
        Permanent jeska = harness.enterBattlefieldAndReturn(player1, new JeskaThriceReborn());
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbilityWithMultiTargets(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(jeska), 1, 1,
                List.of(player1.getId(), player2.getId(), bear.getId()));
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
        assertThat(bear.getMarkedDamage()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Jeska, Thrice Reborn");
    }

    @Test
    void canChooseZeroTargetsAndZeroX() {
        gd.recordCommanderCastFromCommandZone(player1.getId());
        Permanent jeska = harness.enterBattlefieldAndReturn(player1, new JeskaThriceReborn());

        harness.activateAbilityWithMultiTargets(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(jeska), 1, 0, List.of());
        harness.passBothPriorities();

        assertThat(jeska.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(jeska.getLoyaltyActivationsThisTurn()).isEqualTo(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void cannotSpendMoreLoyaltyThanAvailable() {
        gd.recordCommanderCastFromCommandZone(player1.getId());
        Permanent jeska = harness.enterBattlefieldAndReturn(player1, new JeskaThriceReborn());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(jeska), 1, 2,
                List.of(player2.getId()))).isInstanceOf(IllegalStateException.class);

        assertThat(jeska.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentCreatureDoesNotDealTripleDamageToAbilityController() {
        gd.recordCommanderCastFromCommandZone(player1.getId());
        Permanent jeska = harness.enterBattlefieldAndReturn(player1, new JeskaThriceReborn());
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(jeska),
                0, null, bear.getId());
        harness.passBothPriorities();

        declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(bear)));
        resolveCombat(player2);

        harness.assertLife(player1, 18);
    }

    @Test
    void onlyTargetedCreatureDealsTripleDamage() {
        gd.recordCommanderCastFromCommandZone(player1.getId());
        Permanent jeska = harness.enterBattlefieldAndReturn(player1, new JeskaThriceReborn());
        Permanent targeted = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(jeska),
                0, null, targeted.getId());
        harness.passBothPriorities();

        declareAttackers(player1, List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(targeted),
                gd.playerBattlefields.get(player1.getId()).indexOf(other)));
        resolveCombat();

        harness.assertLife(player2, 12);
    }

    @Test
    void triplesTrampleDamageToOpponentWithoutTriplingDamageToBlocker() {
        gd.recordCommanderCastFromCommandZone(player1.getId());
        Permanent jeska = harness.enterBattlefieldAndReturn(player1, new JeskaThriceReborn());
        Permanent attacker = addCreatureReady(player1, new ColossalDreadmaw());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(jeska),
                0, null, attacker.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(attackerIndex));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, attackerIndex)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, attackerIndex,
                Map.of(blocker.getId(), 2, player2.getId(), 4));

        harness.assertLife(player2, 8);
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void doesNotTripleCombatDamageToPlaneswalker() {
        gd.recordCommanderCastFromCommandZone(player1.getId());
        for (int i = 0; i < 5; i++) {
            gd.recordCommanderCastFromCommandZone(player2.getId());
        }
        Permanent jeska = harness.enterBattlefieldAndReturn(player1, new JeskaThriceReborn());
        Permanent defender = harness.enterBattlefieldAndReturn(player2, new JeskaThriceReborn());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(jeska),
                0, null, attacker.getId());
        harness.passBothPriorities();
        attacker.setAttacking(true);
        attacker.setAttackTarget(defender.getId());
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        assertThat(defender.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Jeska, Thrice Reborn");
        harness.assertLife(player2, 20);
    }

    @Test
    void multiplierExpiresAtStartOfControllersNextTurn() {
        gd.recordCommanderCastFromCommandZone(player1.getId());
        Permanent jeska = harness.enterBattlefieldAndReturn(player1, new JeskaThriceReborn());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(jeska),
                0, null, bear.getId());
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(bear)));
        resolveCombat();

        harness.assertLife(player2, 18);
    }
}
