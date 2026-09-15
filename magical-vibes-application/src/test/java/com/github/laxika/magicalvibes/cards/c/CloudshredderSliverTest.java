package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CloudshredderSliver.class, BonescytheSliver.class, GrizzlyBears.class})
class CloudshredderSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Cloudshredder Sliver grants itself flying and haste")
    void grantsKeywordsToItself() {
        Permanent cloudshredderSliver = addCreatureReady(player1, new CloudshredderSliver());

        assertThat(gqs.hasKeyword(gd, cloudshredderSliver, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, cloudshredderSliver, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Grants flying and haste to another Sliver you control")
    void grantsKeywordsToAnotherSliverYouControl() {
        addCreatureReady(player1, new CloudshredderSliver());
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant keywords to an opponent's Sliver")
    void doesNotGrantKeywordsToOpposingSliver() {
        addCreatureReady(player1, new CloudshredderSliver());
        Permanent opponentSliver = addCreatureReady(player2, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant keywords to a non-Sliver creature")
    void doesNotGrantKeywordsToNonSliver() {
        addCreatureReady(player1, new CloudshredderSliver());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Granted keywords are removed when Cloudshredder Sliver leaves")
    void losesKeywordsWhenSourceLeaves() {
        Permanent cloudshredderSliver = addCreatureReady(player1, new CloudshredderSliver());
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(cloudshredderSliver);

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.HASTE)).isFalse();
    }
}
