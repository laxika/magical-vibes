package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KindlyCustomer.class})
class KindlyCustomerTest extends BaseCardTest {

    @Test
    @DisplayName("When Kindly Customer enters, its controller draws a card")
    void drawsCardOnEnter() {
        Card drawnCard = new KindlyCustomer();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new KindlyCustomer()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(drawnCard.getId()));
    }

    @Test
    @DisplayName("The draw waits for the enters trigger to resolve and draws exactly one card")
    void drawUsesTheStackAndDrawsExactlyOneCard() {
        Card topCard = new KindlyCustomer();
        Card nextCard = new KindlyCustomer();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setHand(player1, List.of(new KindlyCustomer()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kindly Customer");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The second player's Kindly Customer draws for its own controller")
    void drawsForTheSecondPlayer() {
        Card drawnCard = new KindlyCustomer();
        harness.forceActivePlayer(player2);
        harness.setLibrary(player2, List.of(drawnCard));
        harness.setHand(player2, List.of(new KindlyCustomer()));
        harness.setHand(player1, List.of());
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
