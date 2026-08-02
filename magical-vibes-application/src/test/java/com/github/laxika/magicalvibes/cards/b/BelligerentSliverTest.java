package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BelligerentSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Belligerent Sliver grants itself menace (it is a Sliver)")
    void grantsSelfMenace() {
        Permanent sliver = addCreatureReady(player1, new BelligerentSliver());

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Grants menace to another Sliver you control")
    void grantsMenaceToOtherSliver() {
        addCreatureReady(player1, new BelligerentSliver());
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant menace to a non-Sliver creature")
    void doesNotGrantToNonSliver() {
        addCreatureReady(player1, new BelligerentSliver());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant menace to an opponent's Sliver")
    void doesNotGrantToOpponentSliver() {
        addCreatureReady(player1, new BelligerentSliver());
        Permanent opponentSliver = addCreatureReady(player2, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.MENACE)).isFalse();
    }
}
