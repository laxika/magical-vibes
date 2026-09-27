package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WingedHiveTyrant.class, GrizzlyBears.class})
class WingedHiveTyrantTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control with counters have flying and haste")
    void counteredOwnCreatureGainsFlyingAndHaste() {
        harness.addToBattlefield(player1, new WingedHiveTyrant());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.CHARGE, 1);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("A creature you control without counters does not gain flying or haste")
    void uncounteredOwnCreatureDoesNotGainKeywords() {
        harness.addToBattlefield(player1, new WingedHiveTyrant());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's countered creature does not gain flying or haste")
    void counteredOpponentCreatureDoesNotGainKeywords() {
        harness.addToBattlefield(player1, new WingedHiveTyrant());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.CHARGE, 1);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }
}
