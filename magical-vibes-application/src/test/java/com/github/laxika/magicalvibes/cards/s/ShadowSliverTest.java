package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShadowSliver.class, BonescytheSliver.class, GrizzlyBears.class})
class ShadowSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Shadow Sliver grants itself shadow")
    void grantsShadowToItself() {
        Permanent shadowSliver = addCreatureReady(player1, new ShadowSliver());

        assertThat(gqs.hasKeyword(gd, shadowSliver, Keyword.SHADOW)).isTrue();
    }

    @Test
    @DisplayName("Grants shadow to another Sliver you control")
    void grantsShadowToAnotherSliver() {
        addCreatureReady(player1, new ShadowSliver());
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.SHADOW)).isTrue();
    }

    @Test
    @DisplayName("Grants shadow to an opponent's Sliver too")
    void grantsShadowToOpponentSliver() {
        addCreatureReady(player1, new ShadowSliver());
        Permanent opponentSliver = addCreatureReady(player2, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.SHADOW)).isTrue();
    }

    @Test
    @DisplayName("Does not grant shadow to a non-Sliver creature")
    void doesNotGrantShadowToNonSliver() {
        addCreatureReady(player1, new ShadowSliver());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.SHADOW)).isFalse();
    }
}
