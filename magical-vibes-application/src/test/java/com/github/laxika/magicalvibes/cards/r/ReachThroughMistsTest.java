package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ReachThroughMists.class)
class ReachThroughMistsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving draws a card")
    void resolvingDrawsACard() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castFromHand(player1, new ReachThroughMists(), "{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        harness.assertInGraveyard(player1, "Reach Through Mists");
    }

    @Test
    @DisplayName("Drawing from an empty deck does not crash")
    void drawFromEmptyDeck() {
        gd.playerDecks.get(player1.getId()).clear();

        harness.castFromHand(player1, new ReachThroughMists(), "{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
