package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PlatedSliver;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CloudshredderSliver.class, PlatedSliver.class, GrizzlyBears.class})
class CloudshredderSliverTest extends BaseCardTest {

    @Test
    void grantsFlyingAndHasteToSliverCreaturesYouControl() {
        Permanent cloudshredder = addCreatureReady(player1, new CloudshredderSliver());
        Permanent sliver = addCreatureReady(player1, new PlatedSliver());
        Permanent nonSliver = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingSliver = addCreatureReady(player2, new PlatedSliver());

        assertThat(gqs.hasKeyword(gd, cloudshredder, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, cloudshredder, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, sliver, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, sliver, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonSliver, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, nonSliver, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingSliver, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingSliver, Keyword.HASTE)).isFalse();
    }

    @Test
    void grantedKeywordsAreRemovedWhenCloudshredderSliverLeaves() {
        Permanent cloudshredder = addCreatureReady(player1, new CloudshredderSliver());
        Permanent sliver = addCreatureReady(player1, new PlatedSliver());

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, sliver, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(cloudshredder);

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, sliver, Keyword.HASTE)).isFalse();
    }
}
