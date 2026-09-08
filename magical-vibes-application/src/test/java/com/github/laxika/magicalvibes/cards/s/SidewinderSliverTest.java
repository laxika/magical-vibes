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

@CardUsed({SidewinderSliver.class, BonescytheSliver.class, GrizzlyBears.class})
class SidewinderSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Sidewinder Sliver grants itself flanking")
    void grantsSelfFlanking() {
        Permanent sliver = addCreatureReady(player1, new SidewinderSliver());

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.FLANKING)).isTrue();
    }

    @Test
    @DisplayName("Grants flanking to another Sliver you control")
    void grantsFlankingToOtherSliver() {
        addCreatureReady(player1, new SidewinderSliver());
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.FLANKING)).isTrue();
    }

    @Test
    @DisplayName("Grants flanking to an opponent's Sliver too")
    void grantsFlankingToOpponentSliver() {
        addCreatureReady(player1, new SidewinderSliver());
        Permanent opponentSliver = addCreatureReady(player2, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.FLANKING)).isTrue();
    }

    @Test
    @DisplayName("Does not grant flanking to a non-Sliver creature")
    void doesNotGrantToNonSliver() {
        addCreatureReady(player1, new SidewinderSliver());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLANKING)).isFalse();
    }
}
