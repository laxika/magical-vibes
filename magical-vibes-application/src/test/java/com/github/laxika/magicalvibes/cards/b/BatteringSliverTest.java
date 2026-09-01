package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BatteringSliver.class, BonescytheSliver.class, GrizzlyBears.class})
class BatteringSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Battering Sliver gives itself and other Slivers trample")
    void grantsTrampleToAllSliversIncludingItself() {
        Permanent batteringSliver = addCreatureReady(player1, new BatteringSliver());
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, batteringSliver, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Battering Sliver gives opposing Slivers trample")
    void grantsTrampleToOpposingSlivers() {
        addCreatureReady(player1, new BatteringSliver());
        Permanent opposingSliver = addCreatureReady(player2, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, opposingSliver, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Battering Sliver does not give trample to non-Slivers")
    void doesNotGrantTrampleToNonSlivers() {
        addCreatureReady(player1, new BatteringSliver());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }
}
