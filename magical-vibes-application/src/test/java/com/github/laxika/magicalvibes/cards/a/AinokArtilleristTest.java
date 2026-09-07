package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(AinokArtillerist.class)
class AinokArtilleristTest extends BaseCardTest {

    @Test
    @DisplayName("Does not have reach without a +1/+1 counter")
    void noReachWithoutCounter() {
        Permanent artillerist = addCreatureReady(player1, new AinokArtillerist());

        assertThat(gqs.hasKeyword(gd, artillerist, Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("Has reach while it has a +1/+1 counter")
    void hasReachWithCounter() {
        Permanent artillerist = addCreatureReady(player1, new AinokArtillerist());
        artillerist.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, artillerist, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Loses reach when its +1/+1 counter is removed")
    void losesReachWhenCounterRemoved() {
        Permanent artillerist = addCreatureReady(player1, new AinokArtillerist());
        artillerist.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.hasKeyword(gd, artillerist, Keyword.REACH)).isTrue();

        artillerist.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThat(gqs.hasKeyword(gd, artillerist, Keyword.REACH)).isFalse();
    }
}
