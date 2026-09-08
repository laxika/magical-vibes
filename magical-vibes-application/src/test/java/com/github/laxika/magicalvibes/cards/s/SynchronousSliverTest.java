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

@CardUsed({SynchronousSliver.class, BonescytheSliver.class, GrizzlyBears.class})
class SynchronousSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Synchronous Sliver grants itself vigilance")
    void grantsSelfVigilance() {
        Permanent sliver = addCreatureReady(player1, new SynchronousSliver());

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Grants vigilance to another Sliver you control")
    void grantsVigilanceToOtherSliver() {
        addCreatureReady(player1, new SynchronousSliver());
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Grants vigilance to an opponent's Sliver too")
    void grantsVigilanceToOpponentSliver() {
        addCreatureReady(player1, new SynchronousSliver());
        Permanent opponentSliver = addCreatureReady(player2, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant vigilance to a non-Sliver creature")
    void doesNotGrantToNonSliver() {
        addCreatureReady(player1, new SynchronousSliver());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
    }
}
