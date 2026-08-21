package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({DeltaBloodflies.class, GrizzlyBears.class})
class DeltaBloodfliesTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking makes each opponent lose 1 life when you control a creature with a counter")
    void attackCausesLifeLossWithCounter() {
        addCreatureReady(player1, new DeltaBloodflies());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.CHARGE, 1);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Attacking does not cause life loss without a creature with a counter")
    void attackDoesNotCauseLifeLossWithoutCounter() {
        addCreatureReady(player1, new DeltaBloodflies());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.assertLife(player2, 19);
    }
}
