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
}
