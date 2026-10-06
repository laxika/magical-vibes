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
        resolveAllTriggers();

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

        rayFillet.setPowerModifier(2);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(rayFillet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void rayFilletTriggersForItsOwnCombatDamage() {
        Permanent rayFillet = addCreatureReady(player1, new RayFilletWaveWarrior());
        rayFillet.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        rayFillet.setAttacking(true);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        resolveCombat();
        resolveAllTriggers();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertLife(player2, 19);
    }

    @Test
    void drawsOnceForEachCounteredCreatureRatherThanEachDamagePoint() {
        harness.addToBattlefield(player1, new RayFilletWaveWarrior());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        addAttackerWithCounter(CounterType.CHARGE);
        addAttackerWithCounter(CounterType.PLUS_ONE_PLUS_ONE);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player2, 15);
    }

    @Test
    void removingCounterAfterDamageDoesNotStopDraw() {
        harness.addToBattlefield(player1, new RayFilletWaveWarrior());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        Permanent attacker = addAttackerWithCounter(CounterType.CHARGE);

        resolveCombat();
        attacker.setCounterCount(CounterType.CHARGE, 0);
        resolveAllTriggers();

        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void opposingCounteredCreatureDoesNotTriggerDraw() {
        harness.addToBattlefield(player1, new RayFilletWaveWarrior());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setCounterCount(CounterType.CHARGE, 1);
        attacker.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 18);
    }

    @Test
    void evolveRechecksStatsWhenTriggerResolves() {
        Permanent rayFillet = harness.addToBattlefieldAndReturn(player1, new RayFilletWaveWarrior());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        rayFillet.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        resolveAllTriggers();

        assertThat(rayFillet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
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
