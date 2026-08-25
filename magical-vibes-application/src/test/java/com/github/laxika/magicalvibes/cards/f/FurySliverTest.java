package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MetallicSliver;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FurySliver.class, MetallicSliver.class, GrizzlyBears.class})
class FurySliverTest extends BaseCardTest {

    @Test
    @DisplayName("Fury Sliver grants itself double strike")
    void grantsDoubleStrikeToItself() {
        Permanent furySliver = addCreatureReady(player1, new FurySliver());

        assertThat(gqs.hasKeyword(gd, furySliver, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Fury Sliver grants double strike to another Sliver")
    void grantsDoubleStrikeToAnotherSliver() {
        addCreatureReady(player1, new FurySliver());
        Permanent sliver = addCreatureReady(player1, new MetallicSliver());

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Fury Sliver grants double strike to an opponent's Sliver")
    void grantsDoubleStrikeToOpposingSliver() {
        addCreatureReady(player1, new FurySliver());
        Permanent sliver = addCreatureReady(player2, new MetallicSliver());

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Fury Sliver does not grant double strike to a non-Sliver")
    void doesNotGrantDoubleStrikeToNonSliver() {
        addCreatureReady(player1, new FurySliver());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isFalse();
    }
}
