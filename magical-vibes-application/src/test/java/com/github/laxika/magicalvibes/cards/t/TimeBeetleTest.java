package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TimeBeetle.class, GrizzlyBears.class})
class TimeBeetleTest extends BaseCardTest {

    @Test
    void timeTravelsWhenItDealsCombatDamageToAPlayer() {
        Permanent beetle = addCreatureReady(player1, new TimeBeetle());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.setCounterCount(CounterType.TIME, 1);
        beetle.setAttacking(true);

        resolveCombat();
        harness.handleListChoice(player1, "ADD");

        assertThat(target.getCounterCount(CounterType.TIME)).isEqualTo(2);
    }
}
