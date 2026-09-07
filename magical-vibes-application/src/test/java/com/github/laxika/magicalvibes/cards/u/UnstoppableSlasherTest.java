package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnstoppableSlasher.class, GrizzlyBears.class})
class UnstoppableSlasherTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage makes a player lose half their life, rounded up")
    void combatDamageMakesPlayerLoseHalfLifeRoundedUp() {
        harness.setLife(player2, 23);
        Permanent slasher = addCreatureReady(player1, new UnstoppableSlasher());
        slasher.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Does not trigger when blocked and no combat damage reaches the player")
    void doesNotTriggerWithoutCombatDamageToPlayer() {
        harness.setLife(player2, 22);
        Permanent slasher = addCreatureReady(player1, new UnstoppableSlasher());
        slasher.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Returns from its first death tapped with two stun counters")
    void returnsFromFirstDeathTappedWithStunCounters() {
        Permanent slasher = harness.addToBattlefieldAndReturn(player1, new UnstoppableSlasher());
        slasher.setMarkedDamage(slasher.getEffectiveToughness());

        harness.runStateBasedActions();
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Unstoppable Slasher");
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.getCounterCount(CounterType.STUN)).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Unstoppable Slasher");
    }

    @Test
    @DisplayName("Stays in the graveyard when it dies with any counter")
    void staysInGraveyardWhenItDiesWithACounter() {
        Permanent slasher = harness.addToBattlefieldAndReturn(player1, new UnstoppableSlasher());
        slasher.setCounterCount(CounterType.STUN, 1);
        slasher.setMarkedDamage(slasher.getEffectiveToughness());

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Unstoppable Slasher"));
        harness.assertInGraveyard(player1, "Unstoppable Slasher");
    }
}
