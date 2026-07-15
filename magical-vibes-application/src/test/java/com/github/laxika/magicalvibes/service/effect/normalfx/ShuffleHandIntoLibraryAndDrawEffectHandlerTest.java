package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.ShuffleHandIntoLibraryAndDrawEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ShuffleHandIntoLibraryAndDrawEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Shuffles hand into library and draws that many cards")
            void shufflesHandAndDraws() {
                Card card = createCard("Windfall");
                Card handCard1 = createCard("Mountain");
                Card handCard2 = createCard("Forest");
                Card handCard3 = createCard("Swamp");
                gd.playerHands.get(player1Id).addAll(List.of(handCard1, handCard2, handCard3));

                ShuffleHandIntoLibraryAndDrawEffect effect = new ShuffleHandIntoLibraryAndDrawEffect();
                StackEntry entry = createEntry(card, player1Id, List.of(effect));

                resolveEffect(gd, entry, new ShuffleHandIntoLibraryAndDrawEffect());

                // Hand should be cleared
                assertThat(gd.playerHands.get(player1Id)).isEmpty();
                // 3 cards moved to library
                assertThat(gd.playerDecks.get(player1Id)).hasSize(3);
                // Draw 3 cards (one per card shuffled)
                verify(drawService, times(3)).resolveDrawCard(gd, player1Id);
            }

            @Test
            @DisplayName("Skips player with empty hand")
            void skipsEmptyHand() {
                Card card = createCard("Windfall");
                ShuffleHandIntoLibraryAndDrawEffect effect = new ShuffleHandIntoLibraryAndDrawEffect();
                StackEntry entry = createEntry(card, player1Id, List.of(effect));
                // Both players have empty hands

                resolveEffect(gd, entry, new ShuffleHandIntoLibraryAndDrawEffect());

                verify(drawService, never()).resolveDrawCard(any(), any());
                verify(gameBroadcastService, times(2)).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) ->
                        logEntry.plainText().contains("no cards in hand")));
            }

            @Test
            @DisplayName("Processes each player independently")
            void processesEachPlayer() {
                Card card = createCard("Windfall");
                gd.playerHands.get(player1Id).addAll(List.of(createCard("A"), createCard("B")));
                gd.playerHands.get(player2Id).addAll(List.of(createCard("C")));

                ShuffleHandIntoLibraryAndDrawEffect effect = new ShuffleHandIntoLibraryAndDrawEffect();
                StackEntry entry = createEntry(card, player1Id, List.of(effect));

                resolveEffect(gd, entry, new ShuffleHandIntoLibraryAndDrawEffect());

                verify(drawService, times(2)).resolveDrawCard(gd, player1Id);
                verify(drawService, times(1)).resolveDrawCard(gd, player2Id);
            }
}
