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
    void grantsSelfFlyingAndHaste() {
        Permanent sliver = addCreatureReady(player1, new CloudshredderSliver());

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, sliver, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Grants flying and haste to another Sliver you control")
    void grantsKeywordsToOtherSliver() {
        addCreatureReady(player1, new CloudshredderSliver());
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant keywords to non-Slivers or opposing Slivers")
    void onlyGrantsToSliversYouControl() {
        addCreatureReady(player1, new CloudshredderSliver());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingSliver = addCreatureReady(player2, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingSliver, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingSliver, Keyword.HASTE)).isFalse();
    }
}
