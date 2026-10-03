package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlackPantherClawsOfBast.class, GrizzlyBears.class})
class BlackPantherClawsOfBastTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on the creature attacking alone")
    void putsCounterOnLoneAttacker() {
        addCreatureReady(player1, new BlackPantherClawsOfBast());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when multiple creatures attack")
    void doesNotTriggerWithMultipleAttackers() {
        Permanent panther = addCreatureReady(player1, new BlackPantherClawsOfBast());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));

        assertThat(panther.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Black Panther, Claws of Bast"));
    }

    @Test
    @DisplayName("Black Panther gets its own counter when attacking alone and gains life for its damage")
    void lonePantherGetsCounterAndLifelink() {
        Permanent panther = addCreatureReady(player1, new BlackPantherClawsOfBast());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveAllTriggers();
        resolveCombat();

        assertThat(panther.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(24);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("An opponent's creature attacking alone does not receive a counter")
    void opponentsLoneAttackerDoesNotTrigger() {
        Permanent panther = addCreatureReady(player1, new BlackPantherClawsOfBast());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(panther.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The lone attacker receives another counter in a later combat")
    void countersAccumulateAcrossCombats() {
        addCreatureReady(player1, new BlackPantherClawsOfBast());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveAllTriggers();
        resolveCombat();
        bears.setTapped(false);
        bears.setAttacking(false);

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
