package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DonatelloMutantMechanic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeroesInAHalfShell.class, DonatelloMutantMechanic.class, HornedTurtle.class, GrizzlyBears.class})
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

    private Permanent addAttacker(Card card) {
        Permanent attacker = addCreatureReady(player1, card);
        attacker.setAttacking(true);
        return attacker;
    }
}
