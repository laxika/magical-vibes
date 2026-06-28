package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DiscardCardEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DiscardCardEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Sets discardCausedByOpponent to false and begins discard")
            void setsDiscardFlag() {
                Card card = createCard("Raven's Crime");
                DiscardCardEffect effect = new DiscardCardEffect(1);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));
                gd.playerHands.get(player1Id).add(createCard("Mountain"));

                resolveEffect(gd, entry, effect);

                assertThat(gd.discardCausedByOpponent).isFalse();
                verify(playerInputService).beginDiscardChoice(gd, player1Id);
            }

            @Test
            @DisplayName("Sets discard remaining count correctly")
            void setsDiscardCount() {
                Card card = createCard("Mind Rot");
                DiscardCardEffect effect = new DiscardCardEffect(2);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));
                gd.playerHands.get(player1Id).addAll(List.of(createCard("A"), createCard("B")));

                resolveEffect(gd, entry, effect);

                assertThat(gd.interaction.revealedHandChoice().discardRemainingCount()).isEqualTo(2);
            }

            @Test
            @DisplayName("Logs message when hand is empty")
            void logsWhenHandEmpty() {
                Card card = createCard("Raven's Crime");
                DiscardCardEffect effect = new DiscardCardEffect(1);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));

                resolveEffect(gd, entry, effect);

                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("no cards to discard")));
                verify(playerInputService, never()).beginDiscardChoice(any(), any());
            }
}
