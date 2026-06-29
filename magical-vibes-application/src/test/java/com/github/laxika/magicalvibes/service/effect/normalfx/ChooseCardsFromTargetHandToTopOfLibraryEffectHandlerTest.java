package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandToTopOfLibraryEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ChooseCardsFromTargetHandToTopOfLibraryEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Reveals hand and begins choice when target has cards")
            void revealsAndBeginsChoice() {
                Card card = createCard("Lapse of Certainty");
                ChooseCardsFromTargetHandToTopOfLibraryEffect effect = new ChooseCardsFromTargetHandToTopOfLibraryEffect(1);
                StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);
                gd.playerHands.get(player2Id).addAll(List.of(createCard("A"), createCard("B")));

                resolveEffect(gd, entry, effect);

                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("looks at") && msg.contains("Player2")));
                verify(playerInputService).beginRevealedHandChoice(eq(gd), eq(player1Id), eq(player2Id), any(), any());
            }

            @Test
            @DisplayName("Logs empty hand when target has no cards")
            void emptyHand() {
                Card card = createCard("Lapse of Certainty");
                ChooseCardsFromTargetHandToTopOfLibraryEffect effect = new ChooseCardsFromTargetHandToTopOfLibraryEffect(1);
                StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

                resolveEffect(gd, entry, effect);

                verify(playerInputService, never()).beginRevealedHandChoice(any(), any(), any(), any(), any());
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("empty")));
            }
}
