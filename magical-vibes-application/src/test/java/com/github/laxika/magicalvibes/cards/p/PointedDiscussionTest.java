package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PointedDiscussion.class, Forest.class, GrizzlyBears.class})
class PointedDiscussionTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards, loses 2 life, and creates a Blood token")
    void drawsLosesLifeAndCreatesBlood() {
        Forest firstDrawnCard = new Forest();
        GrizzlyBears secondDrawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstDrawnCard, secondDrawnCard));
        harness.setHand(player1, List.of(new PointedDiscussion()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDrawnCard, secondDrawnCard);
        harness.assertLife(player1, 18);
        assertThat(countPermanents(player1, "Blood")).isOne();
        harness.assertInGraveyard(player1, "Pointed Discussion");
    }
}
