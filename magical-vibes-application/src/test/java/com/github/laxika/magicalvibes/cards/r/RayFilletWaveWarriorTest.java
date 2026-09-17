package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RayFilletWaveWarrior.class, GrizzlyBears.class})
class RayFilletWaveWarriorTest extends BaseCardTest {

    @Test
    void evolvePutsCounterOnRayFilletWhenLargerCreatureEnters() {
        Permanent rayFillet = harness.addToBattlefieldAndReturn(player1, new RayFilletWaveWarrior());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(rayFillet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void counteredCreatureDealsCombatDamageAndDraws() {
        harness.addToBattlefield(player1, new RayFilletWaveWarrior());
        harness.setHand(player1, List.of());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        Permanent attacker = addAttackerWithCounter(CounterType.CHARGE);

        resolveCombat();
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void creatureWithoutCounterDoesNotTriggerDraw() {
        harness.addToBattlefield(player1, new RayFilletWaveWarrior());
        harness.setHand(player1, List.of());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        addAttackerWithCounter(null);

        resolveCombat();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
    }

    @Test
    void evolveDoesNotTriggerForEqualCreature() {
        Permanent rayFillet = harness.addToBattlefieldAndReturn(player1, new RayFilletWaveWarrior());

        harness.setHand(player1, List.of(new RayFilletWaveWarrior()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(rayFillet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addAttackerWithCounter(CounterType counterType) {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        if (counterType != null) {
            attacker.setCounterCount(counterType, 1);
        }
        attacker.setAttacking(true);
        return attacker;
    }
}
