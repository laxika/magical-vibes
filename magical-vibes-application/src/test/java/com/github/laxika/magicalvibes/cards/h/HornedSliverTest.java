package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.m.MuscleSliver;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HornedSliver.class, MuscleSliver.class, HornedTurtle.class})
class HornedSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Horned Sliver grants itself trample (it is a Sliver)")
    void grantsSelfTrample() {
        Permanent sliver = addCreatureReady(player1, new HornedSliver());

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Grants trample to another Sliver you control")
    void grantsTrampleToOtherSliver() {
        addCreatureReady(player1, new HornedSliver());
        Permanent otherSliver = addCreatureReady(player1, new MuscleSliver());

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Grants trample to an opponent's Sliver too")
    void grantsTrampleToOpponentSliver() {
        addCreatureReady(player1, new HornedSliver());
        Permanent opponentSliver = addCreatureReady(player2, new MuscleSliver());

        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant trample to a non-Sliver creature")
    void doesNotGrantToNonSliver() {
        addCreatureReady(player1, new HornedSliver());
        Permanent turtle = addCreatureReady(player1, new HornedTurtle());

        assertThat(gqs.hasKeyword(gd, turtle, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Stops granting trample when Horned Sliver leaves the battlefield")
    void stopsGrantingWhenSourceLeavesBattlefield() {
        Permanent source = addCreatureReady(player1, new HornedSliver());
        Permanent otherSliver = addCreatureReady(player1, new MuscleSliver());

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.TRAMPLE)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, source));

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.TRAMPLE)).isFalse();
    }
}
