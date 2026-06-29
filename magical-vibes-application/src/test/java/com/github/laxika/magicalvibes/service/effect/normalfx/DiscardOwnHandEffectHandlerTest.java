package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DiscardOwnHandEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Discards all cards from hand to graveyard")
            void discardsAllCards() {
                Card card = createCard("One with Nothing");
                Card handCard1 = createCard("Mountain");
                Card handCard2 = createCard("Forest");
                gd.playerHands.get(player1Id).addAll(List.of(handCard1, handCard2));
                StackEntry entry = createEntry(card, player1Id, List.of(new DiscardOwnHandEffect()));

                resolveEffect(gd, entry, new DiscardOwnHandEffect());

                assertThat(gd.playerHands.get(player1Id)).isEmpty();
                verify(graveyardService).addCardToGraveyard(gd, player1Id, handCard1);
                verify(graveyardService).addCardToGraveyard(gd, player1Id, handCard2);
                verify(triggerCollectionService).checkDiscardTriggers(gd, player1Id, handCard1);
                verify(triggerCollectionService).checkDiscardTriggers(gd, player1Id, handCard2);
            }

            @Test
            @DisplayName("Does nothing with empty hand")
            void doesNothingWithEmptyHand() {
                Card card = createCard("One with Nothing");
                StackEntry entry = createEntry(card, player1Id, List.of(new DiscardOwnHandEffect()));

                resolveEffect(gd, entry, new DiscardOwnHandEffect());

                verify(graveyardService, never()).addCardToGraveyard(any(), any(), any());
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("no cards to discard")));
            }

            @Test
            @DisplayName("Sets discardCausedByOpponent to false")
            void setsDiscardCausedByOpponent() {
                Card card = createCard("One with Nothing");
                gd.playerHands.get(player1Id).add(createCard("Mountain"));
                gd.discardCausedByOpponent = true;
                StackEntry entry = createEntry(card, player1Id, List.of(new DiscardOwnHandEffect()));

                resolveEffect(gd, entry, new DiscardOwnHandEffect());

                assertThat(gd.discardCausedByOpponent).isFalse();
            }

            @Test
            @DisplayName("Logs discard count correctly")
            void logsDiscardCount() {
                Card card = createCard("One with Nothing");
                gd.playerHands.get(player1Id).addAll(List.of(createCard("A"), createCard("B"), createCard("C")));
                StackEntry entry = createEntry(card, player1Id, List.of(new DiscardOwnHandEffect()));

                resolveEffect(gd, entry, new DiscardOwnHandEffect());

                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("discards their hand") && msg.contains("3 cards")));
            }
}
