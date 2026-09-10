package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TuktukRubblefort.class, GrizzlyBears.class})
class TuktukRubblefortTest extends BaseCardTest {

    @Test
    @DisplayName("Grants haste to creatures its controller controls, including itself")
    void grantsHasteToOwnCreatures() {
        Permanent rubblefort = addCreatureReady(player1, new TuktukRubblefort());
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, rubblefort, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownBears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant haste to an opponent's creatures")
    void doesNotGrantHasteToOpponentsCreatures() {
        harness.addToBattlefield(player1, new TuktukRubblefort());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.HASTE)).isFalse();
    }
}
