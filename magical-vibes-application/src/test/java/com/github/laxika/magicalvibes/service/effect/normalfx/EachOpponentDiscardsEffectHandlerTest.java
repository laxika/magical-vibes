package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.EachOpponentDiscardsEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EachOpponentDiscardsEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Only opponents discard, controller is excluded")
            void onlyOpponentsDiscard() {
                Card card = createCard("Hymn to Tourach");
                EachOpponentDiscardsEffect effect = new EachOpponentDiscardsEffect(2);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));
                gd.activePlayerId = player2Id;
                gd.playerHands.get(player2Id).addAll(List.of(createCard("A"), createCard("B")));

                resolveEffect(gd, entry, effect);

                // Player2 (opponent) should be first since they're active player and not controller
                verify(playerInputService).beginDiscardChoice(eq(gd), eq(player2Id), anyInt(),
                        argThat((DiscardFollowUp f) -> f.eachPlayerAmount() == 2));
            }

            @Test
            @DisplayName("Controller is never added to discard queue")
            void controllerExcludedFromQueue() {
                Card card = createCard("Hymn to Tourach");
                EachOpponentDiscardsEffect effect = new EachOpponentDiscardsEffect(1);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));
                gd.activePlayerId = player1Id;
                gd.playerHands.get(player2Id).add(createCard("A"));

                resolveEffect(gd, entry, effect);

                // player2 is the only opponent; active player is controller, so they start
                // with player2 from queue
                verify(playerInputService).beginDiscardChoice(eq(gd), eq(player2Id), anyInt(),
                        any(DiscardFollowUp.class));
            }
}
