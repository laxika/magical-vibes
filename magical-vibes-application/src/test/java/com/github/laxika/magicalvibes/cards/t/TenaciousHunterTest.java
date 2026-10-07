package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Manalith;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TenaciousHunter.class, GrizzlyBears.class, Manalith.class})
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

    @Test
    @DisplayName("A -1/-1 counter on a noncreature does not grant keywords")
    void noncreatureCounterDoesNotQualify() {
        Permanent hunter = harness.addToBattlefieldAndReturn(player1, new TenaciousHunter());
        Permanent manalith = harness.addToBattlefieldAndReturn(player2, new Manalith());
        manalith.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, hunter, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, hunter, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("A +1/+1 counter does not grant keywords")
    void otherCounterTypeDoesNotQualify() {
        Permanent hunter = harness.addToBattlefieldAndReturn(player1, new TenaciousHunter());
        hunter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, hunter, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, hunter, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Another friendly creature with a -1/-1 counter enables both keywords")
    void friendlyCreatureCounterQualifies() {
        Permanent hunter = harness.addToBattlefieldAndReturn(player1, new TenaciousHunter());
        Permanent otherHunter = harness.addToBattlefieldAndReturn(player1, new TenaciousHunter());
        otherHunter.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, hunter, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, hunter, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Keywords disappear when the only creature with a -1/-1 counter dies")
    void losesKeywordsWhenCounterBearerDies() {
        Permanent hunter = harness.addToBattlefieldAndReturn(player1, new TenaciousHunter());
        Permanent otherHunter = harness.addToBattlefieldAndReturn(player2, new TenaciousHunter());
        otherHunter.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, hunter, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, hunter, Keyword.DEATHTOUCH)).isTrue();

        otherHunter.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 4);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(otherHunter);
        assertThat(gqs.hasKeyword(gd, hunter, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, hunter, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Removing one counter bearer does not disable the ability while another remains")
    void retainsKeywordsWhileAnotherCreatureHasCounter() {
        Permanent hunter = harness.addToBattlefieldAndReturn(player1, new TenaciousHunter());
        Permanent otherHunter = harness.addToBattlefieldAndReturn(player2, new TenaciousHunter());
        hunter.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        otherHunter.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        hunter.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);

        assertThat(gqs.hasKeyword(gd, hunter, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, hunter, Keyword.DEATHTOUCH)).isTrue();

        otherHunter.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);

        assertThat(gqs.hasKeyword(gd, hunter, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, hunter, Keyword.DEATHTOUCH)).isFalse();
    }
}
