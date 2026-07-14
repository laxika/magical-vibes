package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ThunderblustTest extends BaseCardTest {

    @Test
    @DisplayName("No trample without a -1/-1 counter")
    void noTrampleWithoutCounter() {
        harness.addToBattlefield(player1, new Thunderblust());

        Permanent thunderblust = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.hasKeyword(gd, thunderblust, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Has trample while it has a -1/-1 counter")
    void hasTrampleWithCounter() {
        harness.addToBattlefield(player1, new Thunderblust());

        Permanent thunderblust = gd.playerBattlefields.get(player1.getId()).getFirst();
        thunderblust.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, thunderblust, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Loses trample when the -1/-1 counter is removed")
    void losesTrampleWhenCounterRemoved() {
        harness.addToBattlefield(player1, new Thunderblust());

        Permanent thunderblust = gd.playerBattlefields.get(player1.getId()).getFirst();
        thunderblust.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        assertThat(gqs.hasKeyword(gd, thunderblust, Keyword.TRAMPLE)).isTrue();

        thunderblust.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);
        assertThat(gqs.hasKeyword(gd, thunderblust, Keyword.TRAMPLE)).isFalse();
    }
}
