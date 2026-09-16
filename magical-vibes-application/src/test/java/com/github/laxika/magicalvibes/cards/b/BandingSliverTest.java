package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BandingSliver.class, BonescytheSliver.class, GrizzlyBears.class})
class BandingSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Banding Sliver grants banding to itself")
    void grantsBandingToSelf() {
        Permanent sliver = addCreatureReady(player1, new BandingSliver());

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.BANDING)).isTrue();
    }

    @Test
    @DisplayName("Grants banding to another Sliver you control")
    void grantsBandingToOtherSliver() {
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());
        addCreatureReady(player1, new BandingSliver());

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.BANDING)).isTrue();
    }

    @Test
    @DisplayName("Does not grant banding to a non-Sliver creature")
    void doesNotGrantToNonSliver() {
        addCreatureReady(player1, new BandingSliver());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.BANDING)).isFalse();
    }

    @Test
    @DisplayName("Does not grant banding to an opponent's Sliver")
    void doesNotGrantToOpponentSliver() {
        addCreatureReady(player1, new BandingSliver());
        Permanent opponentSliver = addCreatureReady(player2, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.BANDING)).isFalse();
    }
}
