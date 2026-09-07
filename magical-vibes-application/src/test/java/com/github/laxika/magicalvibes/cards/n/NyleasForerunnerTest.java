package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NyleasForerunner.class, GrizzlyBears.class})
class NyleasForerunnerTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control have trample")
    void ownCreaturesGainTrample() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new NyleasForerunner());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Opponent creatures do not gain trample")
    void opponentCreaturesDoNotGainTrample() {
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new NyleasForerunner());

        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Trample is removed when Nylea's Forerunner leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent forerunner = addCreatureReady(player1, new NyleasForerunner());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(forerunner);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }
}
