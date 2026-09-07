package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MartyrForTheCause.class, GrizzlyBears.class})
class MartyrForTheCauseTest extends BaseCardTest {

    @Test
    @DisplayName("When Martyr for the Cause dies, it proliferates")
    void proliferatesWhenItDies() {
        Permanent martyr = addCreatureReady(player1, new MartyrForTheCause());
        martyr.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        Permanent creatureWithCounter = addCreatureReady(player2, new GrizzlyBears());
        creatureWithCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        resolveCombat();
        harness.assertInGraveyard(player1, "Martyr for the Cause");

        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(creatureWithCounter.getId()));

        assertThat(creatureWithCounter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
