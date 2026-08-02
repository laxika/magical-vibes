package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HeartSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Heart Sliver grants itself haste (it is a Sliver)")
    void grantsSelfHaste() {
        Permanent sliver = addCreatureReady(player1, new HeartSliver());

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Grants haste to another Sliver you control")
    void grantsHasteToOtherSliver() {
        addCreatureReady(player1, new HeartSliver());
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Grants haste to an opponent's Sliver too")
    void grantsHasteToOpponentSliver() {
        addCreatureReady(player1, new HeartSliver());
        Permanent opponentSliver = addCreatureReady(player2, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant haste to a non-Sliver creature")
    void doesNotGrantToNonSliver() {
        addCreatureReady(player1, new HeartSliver());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }
}
