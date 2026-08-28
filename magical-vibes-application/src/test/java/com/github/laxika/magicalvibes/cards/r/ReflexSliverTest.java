package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SynchronousSliver;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReflexSliver.class, SynchronousSliver.class, GrizzlyBears.class})
class ReflexSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Reflex Sliver grants itself haste")
    void grantsSelfHaste() {
        Permanent sliver = addCreatureReady(player1, new ReflexSliver());

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Grants haste to another Sliver you control")
    void grantsHasteToAnotherSliver() {
        addCreatureReady(player1, new ReflexSliver());
        Permanent otherSliver = addCreatureReady(player1, new SynchronousSliver());

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Grants haste to an opponent's Sliver")
    void grantsHasteToOpponentSliver() {
        addCreatureReady(player1, new ReflexSliver());
        Permanent opponentSliver = addCreatureReady(player2, new SynchronousSliver());

        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant haste to a non-Sliver creature")
    void doesNotGrantToNonSliver() {
        addCreatureReady(player1, new ReflexSliver());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }
}
