package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DonatelloMutantMechanic;
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

@CardUsed({HeroesInAHalfShell.class, DonatelloMutantMechanic.class, HornedTurtle.class, GrizzlyBears.class,
        HealingSalve.class})
class HeroesInAHalfShellTest extends BaseCardTest {

    @Test
    void matchingCombatDamagePutsCountersOnEachMatchingDealerAndDrawsOnce() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addCreatureReady(player1, new HeroesInAHalfShell());
        Permanent mutant = addAttacker(new DonatelloMutantMechanic());
        Permanent turtle = addAttacker(new HornedTurtle());
        Permanent nonMatching = addAttacker(new GrizzlyBears());

        resolveCombat();
        resolveAllTriggers();

        assertThat(mutant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(turtle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonMatching.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void nonMatchingCombatDamageDoesNotTrigger() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addCreatureReady(player1, new HeroesInAHalfShell());
        Permanent nonMatching = addAttacker(new GrizzlyBears());

        resolveCombat();

        assertThat(nonMatching.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void sourceItselfGetsOneCounterDespiteMatchingThreeSubtypes() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        Permanent heroes = addAttacker(new HeroesInAHalfShell());

        resolveCombat();
        resolveAllTriggers();

        assertThat(heroes.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertLife(player2, 15);
    }

    @Test
    void opposingTurtleCombatDamageDoesNotTrigger() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addCreatureReady(player1, new HeroesInAHalfShell());
        Permanent turtle = addCreatureReady(player2, new HornedTurtle());
        turtle.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(turtle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 19);
    }

    @Test
    void drawStillHappensWhenSourceAndOnlyDealerLeaveBeforeResolution() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        Permanent heroes = addAttacker(new HeroesInAHalfShell());

        resolveCombat();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(heroes);
        harness.setGraveyard(player1, List.of(heroes.getCard()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(heroes.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void fullyPreventedDealerDoesNotGetCounterWhenAnotherDealerConnects() {
        harness.setHand(player1, List.of(new HealingSalve()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castModalInstant(player1, 0, 1, List.of(player2.getId()));
        resolveAllTriggers();

        addCreatureReady(player1, new HeroesInAHalfShell());
        Permanent turtle = addAttacker(new HornedTurtle());
        Permanent mutant = addAttacker(new DonatelloMutantMechanic());

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        assertThat(turtle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(mutant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private Permanent addAttacker(Card card) {
        Permanent attacker = addCreatureReady(player1, card);
        attacker.setAttacking(true);
        return attacker;
    }
}
