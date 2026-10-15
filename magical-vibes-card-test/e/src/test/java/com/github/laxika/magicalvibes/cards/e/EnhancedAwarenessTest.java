package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EnhancedAwareness.class, Forest.class})
class EnhancedAwarenessTest extends BaseCardTest {

    @Test
    @DisplayName("Draws three cards, then makes the controller discard a card")
    void drawsThreeThenDiscards() {
        Forest originalCard = new Forest();
        harness.setHand(player1, List.of(new EnhancedAwareness(), originalCard));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(originalCard);
    }

    @Test
    @DisplayName("Can discard a newly drawn card when the spell was the only card in hand")
    void discardsNewlyDrawnCardFromInitiallyEmptyHand() {
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        Forest remaining = new Forest();
        Forest opponentCard = new Forest();
        harness.setHand(player1, List.of(new EnhancedAwareness()));
        harness.setHand(player2, List.of(opponentCard));
        harness.setLibrary(player1, List.of(first, second, third, remaining));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second, third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(second);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Enhanced Awareness");
    }
}
