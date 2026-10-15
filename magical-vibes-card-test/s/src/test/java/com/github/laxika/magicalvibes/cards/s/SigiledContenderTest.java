package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SigiledContender.class)
class SigiledContenderTest extends BaseCardTest {

    @Test
    @DisplayName("Does not have lifelink without a +1/+1 counter")
    void noLifelinkWithoutCounter() {
        Permanent contender = addCreatureReady(player1, new SigiledContender());

        assertThat(gqs.hasKeyword(gd, contender, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Has lifelink while it has a +1/+1 counter")
    void hasLifelinkWithCounter() {
        Permanent contender = addCreatureReady(player1, new SigiledContender());
        contender.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, contender, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Loses lifelink when its +1/+1 counter is removed")
    void losesLifelinkWhenCounterRemoved() {
        Permanent contender = addCreatureReady(player1, new SigiledContender());
        contender.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.hasKeyword(gd, contender, Keyword.LIFELINK)).isTrue();

        contender.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThat(gqs.hasKeyword(gd, contender, Keyword.LIFELINK)).isFalse();
    }
}
