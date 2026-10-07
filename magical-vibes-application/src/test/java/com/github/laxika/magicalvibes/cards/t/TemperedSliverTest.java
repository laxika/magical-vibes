package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GaleriderSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TemperedSliver.class, GrizzlyBears.class, GaleriderSliver.class})
class TemperedSliverTest extends BaseCardTest {

    @Test
    void givesCombatDamageCounterAbilityToControlledSliversOnly() {
        Permanent temperedSliver = addCreatureReady(player1, new TemperedSliver());
        Permanent otherSliver = addCreatureReady(player1, new GaleriderSliver());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        temperedSliver.setAttacking(true);
        otherSliver.setAttacking(true);
        bear.setAttacking(true);

        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(temperedSliver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(otherSliver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotGrantAbilityToOpposingSlivers() {
        addCreatureReady(player1, new TemperedSliver());
        Permanent opponentSliver = addCreatureReady(player2, new GaleriderSliver());
        opponentSliver.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(opponentSliver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void multipleTemperedSliversGrantSeparateTriggers() {
        Permanent first = addCreatureReady(player1, new TemperedSliver());
        Permanent second = addCreatureReady(player1, new TemperedSliver());
        first.setAttacking(true);
        second.setAttacking(true);

        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    void removingTemperedSliverBeforeDamageRemovesGrantedAbility() {
        Permanent temperedSliver = addCreatureReady(player1, new TemperedSliver());
        Permanent attacker = addCreatureReady(player1, new GaleriderSliver());
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).remove(temperedSliver);
        gd.playerGraveyards.get(player1.getId()).add(temperedSliver.getCard());

        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void grantedTriggerStillResolvesAfterTemperedSliverLeaves() {
        Permanent temperedSliver = addCreatureReady(player1, new TemperedSliver());
        Permanent attacker = addCreatureReady(player1, new GaleriderSliver());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        gd.playerBattlefields.get(player1.getId()).remove(temperedSliver);
        gd.playerGraveyards.get(player1.getId()).add(temperedSliver.getCard());
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void combatDamageToCreatureDoesNotPutCounterOnSliver() {
        Permanent attacker = addCreatureReady(player1, new TemperedSliver());
        addCreatureReady(player2, new GaleriderSliver());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat(player1);
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
