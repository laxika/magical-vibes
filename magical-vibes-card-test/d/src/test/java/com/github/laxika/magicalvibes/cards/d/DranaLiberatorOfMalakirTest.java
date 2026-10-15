package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DranaLiberatorOfMalakir.class, AirElemental.class, GrizzlyBears.class})
class DranaLiberatorOfMalakirTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on each attacking creature you control")
    void putsCountersOnEachAttackingCreatureYouControl() {
        Permanent drana = addCreatureReady(player1, new DranaLiberatorOfMalakir());
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        resolveCombat();
        resolveAllTriggers();

        assertThat(drana.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(otherAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger when Drana deals no combat damage to a player")
    void doesNotTriggerWithoutCombatDamageToPlayer() {
        Permanent drana = addCreatureReady(player1, new DranaLiberatorOfMalakir());
        addCreatureReady(player2, new AirElemental());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(drana.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Counters from first-strike damage increase other attackers' regular damage")
    void countersIncreaseRegularCombatDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new DranaLiberatorOfMalakir());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveCombat();
        resolveAllTriggers();
        harness.passBothPriorities();

        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Blocked attackers receive counters before regular combat damage")
    void blockedAttackerSurvivesWithCounter() {
        Permanent drana = addCreatureReady(player1, new DranaLiberatorOfMalakir());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        resolveCombat();
        resolveAllTriggers();
        harness.passBothPriorities();

        assertThat(drana.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(blocker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }
}
