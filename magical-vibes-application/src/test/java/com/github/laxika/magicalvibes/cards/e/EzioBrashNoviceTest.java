package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(EzioBrashNovice.class)
class EzioBrashNoviceTest extends BaseCardTest {

    @Test
    void attackingPutsPlusOneCounterOnEzio() {
        Permanent ezio = addCreatureReady(player1, new EzioBrashNovice());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(ezio.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void twoCountersGiveEzioFirstStrikeAndAssassin() {
        Permanent ezio = addCreatureReady(player1, new EzioBrashNovice());
        ezio.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        ezio.setCounterCount(CounterType.STUN, 1);

        assertThat(gqs.hasKeyword(gd, ezio, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, ezio, CardSubtype.ASSASSIN)).isTrue();
    }

    @Test
    void fewerThanTwoCountersDoNotGiveEzioTheBonus() {
        Permanent ezio = addCreatureReady(player1, new EzioBrashNovice());
        ezio.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, ezio, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, ezio, CardSubtype.ASSASSIN)).isFalse();
    }
}
