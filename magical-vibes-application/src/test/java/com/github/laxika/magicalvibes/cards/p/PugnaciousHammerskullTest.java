package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PugnaciousHammerskull.class, PygmyAllosaurus.class, GrizzlyBears.class})
class PugnaciousHammerskullTest extends BaseCardTest {

    @Test
    void putsStunCounterOnItselfWhenAttackingWithoutAnotherDinosaur() {
        Permanent hammerskull = addCreatureReady(player1, new PugnaciousHammerskull());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(hammerskull.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    void doesNotPutStunCounterOnItselfWhenControllingAnotherDinosaur() {
        Permanent hammerskull = addCreatureReady(player1, new PugnaciousHammerskull());
        addCreatureReady(player1, new PygmyAllosaurus());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(hammerskull.getCounterCount(CounterType.STUN)).isZero();
    }

    @Test
    void anotherNonDinosaurDoesNotPreventTheStunCounter() {
        Permanent hammerskull = addCreatureReady(player1, new PugnaciousHammerskull());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(hammerskull.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    void opponentDinosaurDoesNotPreventTheStunCounter() {
        Permanent hammerskull = addCreatureReady(player1, new PugnaciousHammerskull());
        addCreatureReady(player2, new PygmyAllosaurus());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(hammerskull.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }
}
