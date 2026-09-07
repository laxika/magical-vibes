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

@CardUsed({SpittingSliver.class, BonescytheSliver.class, GrizzlyBears.class})
class SpittingSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Spitting Sliver grants itself first strike")
    void grantsSelfFirstStrike() {
        Permanent sliver = addCreatureReady(player1, new SpittingSliver());

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Grants first strike to Slivers controlled by either player")
    void grantsFirstStrikeToAllSlivers() {
        addCreatureReady(player1, new SpittingSliver());
        Permanent ownSliver = addCreatureReady(player1, new BonescytheSliver());
        Permanent opponentSliver = addCreatureReady(player2, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, ownSliver, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant first strike to a non-Sliver creature")
    void doesNotGrantToNonSliver() {
        addCreatureReady(player1, new SpittingSliver());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isFalse();
    }
}
