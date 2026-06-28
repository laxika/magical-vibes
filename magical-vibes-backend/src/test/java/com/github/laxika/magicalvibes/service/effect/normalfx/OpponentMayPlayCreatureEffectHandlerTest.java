package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.OpponentMayPlayCreatureEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OpponentMayPlayCreatureEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Logs when opponent has no creatures in hand")
            void logsWhenOpponentHasNoCreatures() {
                Card card = createCard("Hunted Wumpus");
                StackEntry entry = createEntry(card, player1Id, List.of());

                when(gameQueryService.getOpponentId(gd, player1Id)).thenReturn(player2Id);

                resolveEffect(gd, entry, new OpponentMayPlayCreatureEffect());

                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("no creature cards in hand")));
            }

            @Test
            @DisplayName("Begins card choice when opponent has creatures")
            void beginsCardChoiceWhenCreaturesAvailable() {
                Card card = createCard("Hunted Wumpus");
                StackEntry entry = createEntry(card, player1Id, List.of());
                Card creatureCard = createCard("Grizzly Bears");
                creatureCard.setType(CardType.CREATURE);
                gd.playerHands.get(player2Id).add(creatureCard);

                when(gameQueryService.getOpponentId(gd, player1Id)).thenReturn(player2Id);

                resolveEffect(gd, entry, new OpponentMayPlayCreatureEffect());

                verify(playerInputService).beginCardChoice(eq(gd), eq(player2Id), any(), any());
            }
}
