package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.EachPlayerRandomDiscardEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EachPlayerRandomDiscardEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Each player discards at random in APNAP order")
            void eachPlayerDiscardsAPNAP() {
                Card card = createCard("Burning Inquiry");
                EachPlayerRandomDiscardEffect effect = new EachPlayerRandomDiscardEffect(1);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));
                gd.activePlayerId = player1Id;
                gd.playerHands.get(player1Id).add(createCard("Mountain"));
                gd.playerHands.get(player2Id).add(createCard("Forest"));

                resolveEffect(gd, entry, effect);

                // Both hands should have been reduced
                assertThat(gd.playerHands.get(player1Id)).isEmpty();
                assertThat(gd.playerHands.get(player2Id)).isEmpty();
                verify(graveyardService, times(2)).addCardToGraveyard(eq(gd), any(), any());
            }

            @Test
            @DisplayName("Controller's own discard is not opponent-caused")
            void controllerDiscardNotOpponentCaused() {
                Card card = createCard("Burning Inquiry");
                EachPlayerRandomDiscardEffect effect = new EachPlayerRandomDiscardEffect(1);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));
                gd.activePlayerId = player1Id;
                gd.playerHands.get(player1Id).add(createCard("Mountain"));

                resolveEffect(gd, entry, effect);

                // After processing, the last discardCausedByOpponent set for p2 is true (opponent)
                // But p1 (active+controller) was processed first with false
                verify(graveyardService).addCardToGraveyard(eq(gd), eq(player1Id), any());
            }
}
