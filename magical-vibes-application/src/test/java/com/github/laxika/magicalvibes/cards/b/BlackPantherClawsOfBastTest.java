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
}
