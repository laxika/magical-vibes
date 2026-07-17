package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerRandomDiscardOrControllerDrawsEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TargetPlayerRandomDiscardOrControllerDrawsEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Controller draws a card when target's hand is empty")
            void controllerDrawsWhenTargetHandEmpty() {
                Card card = createCard("Blazing Specter");
                TargetPlayerRandomDiscardOrControllerDrawsEffect effect = new TargetPlayerRandomDiscardOrControllerDrawsEffect();
                StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

                resolveEffect(gd, entry, new TargetPlayerRandomDiscardOrControllerDrawsEffect());

                verify(drawService).resolveDrawCard(gd, player1Id);
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) ->
                        logEntry.plainText().contains("no cards to discard") && logEntry.plainText().contains("draws a card")));
            }

            @Test
            @DisplayName("Target player discards at random when hand is not empty")
            void targetDiscardsWhenHandNotEmpty() {
                Card card = createCard("Blazing Specter");
                TargetPlayerRandomDiscardOrControllerDrawsEffect effect = new TargetPlayerRandomDiscardOrControllerDrawsEffect();
                StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);
                Card handCard = createCard("Mountain");
                gd.playerHands.get(player2Id).add(handCard);

                resolveEffect(gd, entry, new TargetPlayerRandomDiscardOrControllerDrawsEffect());

                assertThat(gd.discardCausedByOpponent).isTrue();
                // Random discard removes from hand and sends to graveyard
                assertThat(gd.playerHands.get(player2Id)).isEmpty();
                verify(graveyardService).discardCard(eq(gd), eq(player2Id), any());
            }
}
