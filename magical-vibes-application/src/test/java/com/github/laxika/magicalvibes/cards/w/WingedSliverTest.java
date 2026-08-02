package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WingedSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Winged Sliver grants itself flying (it is a Sliver)")
    void grantsSelfFlying() {
        Permanent sliver = addCreatureReady(player1, new WingedSliver());

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Grants flying to another Sliver you control")
    void grantsFlyingToOtherSliver() {
        addCreatureReady(player1, new WingedSliver());
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Grants flying to an opponent's Sliver too")
    void grantsFlyingToOpponentSliver() {
        addCreatureReady(player1, new WingedSliver());
        Permanent opponentSliver = addCreatureReady(player2, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Does not grant flying to a non-Sliver creature")
    void doesNotGrantToNonSliver() {
        addCreatureReady(player1, new WingedSliver());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }
}
