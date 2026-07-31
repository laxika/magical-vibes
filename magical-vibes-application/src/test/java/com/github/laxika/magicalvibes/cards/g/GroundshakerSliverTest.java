package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GroundshakerSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Groundshaker Sliver grants itself trample (it is a Sliver)")
    void grantsSelfTrample() {
        Permanent sliver = addCreatureReady(player1, new GroundshakerSliver());

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Grants trample to another Sliver you control")
    void grantsTrampleToOtherSliver() {
        addCreatureReady(player1, new GroundshakerSliver());
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant trample to a non-Sliver creature")
    void doesNotGrantToNonSliver() {
        addCreatureReady(player1, new GroundshakerSliver());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant trample to an opponent's Sliver")
    void doesNotGrantToOpponentSliver() {
        addCreatureReady(player1, new GroundshakerSliver());
        Permanent opponentSliver = addCreatureReady(player2, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.TRAMPLE)).isFalse();
    }
}
