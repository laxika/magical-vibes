package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Lightwalker.class)
class LightwalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Has flying exactly while it has a +1/+1 counter")
    void hasFlyingWhileItHasPlusOneCounter() {
        Permanent lightwalker = harness.addToBattlefieldAndReturn(player1, new Lightwalker());

        assertThat(gqs.hasKeyword(gd, lightwalker, Keyword.FLYING)).isFalse();

        lightwalker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.hasKeyword(gd, lightwalker, Keyword.FLYING)).isTrue();

        lightwalker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        assertThat(gqs.hasKeyword(gd, lightwalker, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Other counter types do not grant flying")
    void otherCounterTypesDoNotGrantFlying() {
        Permanent lightwalker = harness.addToBattlefieldAndReturn(player1, new Lightwalker());
        lightwalker.setCounterCount(CounterType.CHARGE, 1);

        assertThat(gqs.hasKeyword(gd, lightwalker, Keyword.FLYING)).isFalse();
    }
}
