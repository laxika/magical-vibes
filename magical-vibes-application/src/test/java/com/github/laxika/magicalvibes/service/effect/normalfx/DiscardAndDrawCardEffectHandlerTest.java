package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DiscardAndDrawCardEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Begins discard then stores pending draw count")
            void beginsDiscardAndStoresDrawCount() {
                Card card = createCard("Faithless Looting");
                DiscardAndDrawCardEffect effect = new DiscardAndDrawCardEffect(1, 2);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));
                gd.playerHands.get(player1Id).add(createCard("Mountain"));

                resolveEffect(gd, entry, effect);

                assertThat(gd.pendingRummageDrawCount).isEqualTo(2);
                assertThat(gd.discardCausedByOpponent).isFalse();
                verify(playerInputService).beginDiscardChoice(eq(gd), eq(player1Id), anyInt());
            }

            @Test
            @DisplayName("Does nothing when hand is empty")
            void doesNothingWhenHandEmpty() {
                Card card = createCard("Faithless Looting");
                DiscardAndDrawCardEffect effect = new DiscardAndDrawCardEffect(1, 2);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));

                resolveEffect(gd, entry, effect);

                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("no cards to discard")));
                verify(playerInputService, never()).beginDiscardChoice(any(), any(), anyInt());
            }
}
