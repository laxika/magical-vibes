package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerRandomDiscardXEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TargetPlayerRandomDiscardXEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("No discard when X is 0")
            void noDiscardWhenXIsZero() {
                Card card = createCard("Mind Shatter");
                TargetPlayerRandomDiscardXEffect effect = new TargetPlayerRandomDiscardXEffect();
                StackEntry entry = createEntryWithXValueAndTarget(card, player1Id, List.of(effect), 0, player2Id);

                resolveEffect(gd, entry, new TargetPlayerRandomDiscardXEffect());

                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("discards 0 cards")));
                verify(graveyardService, never()).addCardToGraveyard(any(), any(), any());
            }

            @Test
            @DisplayName("Discards X cards at random")
            void discardsXCardsAtRandom() {
                Card card = createCard("Mind Shatter");
                TargetPlayerRandomDiscardXEffect effect = new TargetPlayerRandomDiscardXEffect();
                StackEntry entry = createEntryWithXValueAndTarget(card, player1Id, List.of(effect), 2, player2Id);
                gd.playerHands.get(player2Id).addAll(List.of(createCard("A"), createCard("B"), createCard("C")));

                resolveEffect(gd, entry, new TargetPlayerRandomDiscardXEffect());

                assertThat(gd.discardCausedByOpponent).isTrue();
                // 2 cards should have been discarded
                assertThat(gd.playerHands.get(player2Id)).hasSize(1);
                verify(graveyardService, times(2)).addCardToGraveyard(eq(gd), eq(player2Id), any());
            }
}
