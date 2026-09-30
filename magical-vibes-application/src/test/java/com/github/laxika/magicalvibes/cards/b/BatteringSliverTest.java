package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AetherMembrane;
import com.github.laxika.magicalvibes.cards.s.SynchronousSliver;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BatteringSliver.class, SynchronousSliver.class, AetherMembrane.class})
class BatteringSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Battering Sliver gives itself and other Slivers trample")
    void grantsTrampleToAllSliversIncludingItself() {
        Permanent batteringSliver = addCreatureReady(player1, new BatteringSliver());
        Permanent otherSliver = addCreatureReady(player1, new SynchronousSliver());

        assertThat(gqs.hasKeyword(gd, batteringSliver, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Battering Sliver gives opposing Slivers trample")
    void grantsTrampleToOpposingSlivers() {
        addCreatureReady(player1, new BatteringSliver());
        Permanent opposingSliver = addCreatureReady(player2, new SynchronousSliver());

        assertThat(gqs.hasKeyword(gd, opposingSliver, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Battering Sliver does not give trample to non-Slivers")
    void doesNotGrantTrampleToNonSlivers() {
        addCreatureReady(player1, new BatteringSliver());
        Permanent nonSliver = addCreatureReady(player1, new AetherMembrane());

        assertThat(gqs.hasKeyword(gd, nonSliver, Keyword.TRAMPLE)).isFalse();
    }
}
