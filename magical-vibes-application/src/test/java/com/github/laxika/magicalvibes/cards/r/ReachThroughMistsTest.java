package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReachThroughMistsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving draws a card")
    void resolvingDrawsACard() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setHand(player1, List.of(new ReachThroughMists()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        harness.assertInGraveyard(player1, "Reach Through Mists");
    }

    @Test
    @DisplayName("Drawing from an empty deck does not crash")
    void drawFromEmptyDeck() {
        gd.playerDecks.get(player1.getId()).clear();

        harness.setHand(player1, List.of(new ReachThroughMists()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
