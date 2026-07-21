package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TenaciousHunterTest extends BaseCardTest {

    @Test
    @DisplayName("No vigilance/deathtouch when no creature has a -1/-1 counter")
    void noKeywordsWithoutCounter() {
        Permanent hunter = harness.addToBattlefieldAndReturn(player1, new TenaciousHunter());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, hunter, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, hunter, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Gains vigilance and deathtouch when an opponent's creature has a -1/-1 counter")
    void gainsKeywordsFromOpponentCreatureCounter() {
        Permanent hunter = harness.addToBattlefieldAndReturn(player1, new TenaciousHunter());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, hunter, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, hunter, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Gains vigilance and deathtouch when it itself has a -1/-1 counter")
    void gainsKeywordsFromOwnCounter() {
        Permanent hunter = harness.addToBattlefieldAndReturn(player1, new TenaciousHunter());
        hunter.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, hunter, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, hunter, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Loses vigilance and deathtouch when the last -1/-1 counter is removed")
    void losesKeywordsWhenCounterRemoved() {
        Permanent hunter = harness.addToBattlefieldAndReturn(player1, new TenaciousHunter());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, hunter, Keyword.VIGILANCE)).isTrue();

        bears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);

        assertThat(gqs.hasKeyword(gd, hunter, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, hunter, Keyword.DEATHTOUCH)).isFalse();
    }
}
