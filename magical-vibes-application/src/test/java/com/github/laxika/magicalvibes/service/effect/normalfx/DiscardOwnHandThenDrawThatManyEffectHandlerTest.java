package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenDrawThatManyEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DiscardOwnHandThenDrawThatManyEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Discards all cards from hand then draws that many")
            void discardsAllThenDraws() {
                Card card = createCard("Shattered Perception");
                Card handCard1 = createCard("Mountain");
                Card handCard2 = createCard("Forest");
                gd.playerHands.get(player1Id).addAll(List.of(handCard1, handCard2));
                StackEntry entry = createEntry(card, player1Id, List.of(new DiscardOwnHandThenDrawThatManyEffect()));

                resolveEffect(gd, entry, new DiscardOwnHandThenDrawThatManyEffect());

                assertThat(gd.playerHands.get(player1Id)).isEmpty();
                verify(graveyardService).addCardToGraveyard(gd, player1Id, handCard1);
                verify(graveyardService).addCardToGraveyard(gd, player1Id, handCard2);
                verify(drawService, times(2)).resolveDrawCard(gd, player1Id);
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) ->
                        logEntry.plainText().contains("draws 2 cards")));
            }

            @Test
            @DisplayName("Does nothing with empty hand")
            void doesNothingWithEmptyHand() {
                Card card = createCard("Shattered Perception");
                StackEntry entry = createEntry(card, player1Id, List.of(new DiscardOwnHandThenDrawThatManyEffect()));

                resolveEffect(gd, entry, new DiscardOwnHandThenDrawThatManyEffect());

                verify(graveyardService, never()).addCardToGraveyard(any(), any(), any());
                verify(drawService, never()).resolveDrawCard(any(), any());
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) ->
                        logEntry.plainText().contains("no cards to discard")));
            }
}
