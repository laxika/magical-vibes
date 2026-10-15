package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BalothPup.class)
class BalothPupTest extends BaseCardTest {

    @Test
    @DisplayName("Gains trample while it has a +1/+1 counter")
    void trampleIsGrantedByPlusOnePlusOneCounter() {
        Permanent balothPup = harness.addToBattlefieldAndReturn(player1, new BalothPup());

        assertThat(gqs.hasKeyword(gd, balothPup, Keyword.TRAMPLE)).isFalse();

        balothPup.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.hasKeyword(gd, balothPup, Keyword.TRAMPLE)).isTrue();

        balothPup.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        assertThat(gqs.hasKeyword(gd, balothPup, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Other counter types do not enable trample")
    void otherCounterTypesDoNotGrantTrample() {
        Permanent balothPup = harness.addToBattlefieldAndReturn(player1, new BalothPup());
        balothPup.setCounterCount(CounterType.PLUS_ONE_PLUS_ZERO, 2);
        balothPup.setCounterCount(CounterType.CHARGE, 1);

        assertThat(gqs.hasKeyword(gd, balothPup, Keyword.TRAMPLE)).isFalse();

        balothPup.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        assertThat(gqs.hasKeyword(gd, balothPup, Keyword.TRAMPLE)).isTrue();

        balothPup.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.hasKeyword(gd, balothPup, Keyword.TRAMPLE)).isTrue();

        balothPup.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        assertThat(gqs.hasKeyword(gd, balothPup, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Only the Pup with a +1/+1 counter gains trample")
    void countersAndTrampleAreLocalToEachPup() {
        Permanent counteredPup = harness.addToBattlefieldAndReturn(player1, new BalothPup());
        Permanent otherPup = harness.addToBattlefieldAndReturn(player1, new BalothPup());
        Permanent opposingPup = harness.addToBattlefieldAndReturn(player2, new BalothPup());
        counteredPup.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, counteredPup, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherPup, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingPup, Keyword.TRAMPLE)).isFalse();

        opposingPup.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.hasKeyword(gd, opposingPup, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherPup, Keyword.TRAMPLE)).isFalse();
    }
}
