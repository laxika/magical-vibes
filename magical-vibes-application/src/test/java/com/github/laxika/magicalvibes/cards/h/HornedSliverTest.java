package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HornedSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Horned Sliver grants itself trample (it is a Sliver)")
    void grantsSelfTrample() {
        Permanent sliver = addCreatureReady(player1, new HornedSliver());

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Grants trample to another Sliver you control")
    void grantsTrampleToOtherSliver() {
        addCreatureReady(player1, new HornedSliver());
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Grants trample to an opponent's Sliver too")
    void grantsTrampleToOpponentSliver() {
        addCreatureReady(player1, new HornedSliver());
        Permanent opponentSliver = addCreatureReady(player2, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant trample to a non-Sliver creature")
    void doesNotGrantToNonSliver() {
        addCreatureReady(player1, new HornedSliver());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }
}
