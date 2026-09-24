package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RisonaAsariCommander.class, GrizzlyBears.class, LightningBolt.class})
class RisonaAsariCommanderTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to an opponent puts an indestructible counter on Risona")
    void combatDamagePutsIndestructibleCounterOnRisona() {
        Permanent risona = addCreatureReady(player1, new RisonaAsariCommander());
        risona.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(risona.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Risona does not get another indestructible counter while she has one")
    void doesNotPutAnotherCounterWhenOneIsAlreadyPresent() {
        Permanent risona = addCreatureReady(player1, new RisonaAsariCommander());
        risona.setCounterCount(CounterType.INDESTRUCTIBLE, 1);
        risona.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(risona.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Combat damage to Risona's controller removes an indestructible counter")
    void combatDamageToControllerRemovesCounter() {
        Permanent risona = harness.addToBattlefieldAndReturn(player1, new RisonaAsariCommander());
        risona.setCounterCount(CounterType.INDESTRUCTIBLE, 1);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(risona.getCounterCount(CounterType.INDESTRUCTIBLE)).isZero();
    }

    @Test
    @DisplayName("Noncombat damage to Risona's controller does not remove an indestructible counter")
    void noncombatDamageToControllerDoesNotRemoveCounter() {
        Permanent risona = harness.addToBattlefieldAndReturn(player1, new RisonaAsariCommander());
        risona.setCounterCount(CounterType.INDESTRUCTIBLE, 1);
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(risona.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
    }
}
