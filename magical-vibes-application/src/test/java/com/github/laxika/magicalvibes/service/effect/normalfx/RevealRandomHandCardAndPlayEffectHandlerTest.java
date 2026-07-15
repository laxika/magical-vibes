package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.RevealRandomHandCardAndPlayEffect;
import com.github.laxika.magicalvibes.networking.message.RevealHandMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RevealRandomHandCardAndPlayEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Logs when target hand is empty")
            void emptyHand() {
                Card card = createCard("Wild Evocation");
                RevealRandomHandCardAndPlayEffect effect = new RevealRandomHandCardAndPlayEffect();
                StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

                resolveEffect(gd, entry, new RevealRandomHandCardAndPlayEffect());

                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) ->
                        logEntry.plainText().contains("no cards in hand")));
            }

            @Test
            @DisplayName("Puts land directly onto the battlefield")
            void putsLandOntoBattlefield() {
                Card card = createCard("Wild Evocation");
                RevealRandomHandCardAndPlayEffect effect = new RevealRandomHandCardAndPlayEffect();
                StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

                Card landCard = createCard("Forest");
                landCard.setType(CardType.LAND);
                gd.playerHands.get(player2Id).add(landCard);
                CardView mockView = mock(CardView.class);
                when(cardViewFactory.create(landCard)).thenReturn(mockView);

                resolveEffect(gd, entry, new RevealRandomHandCardAndPlayEffect());

                verify(battlefieldEntryService).putPermanentOntoBattlefield(eq(gd), eq(player2Id), any(Permanent.class));
                assertThat(gd.playerHands.get(player2Id)).isEmpty();
            }

            @Test
            @DisplayName("Reveals card to all players")
            void revealsToAllPlayers() {
                Card card = createCard("Wild Evocation");
                RevealRandomHandCardAndPlayEffect effect = new RevealRandomHandCardAndPlayEffect();
                StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

                Card revealedCard = createCard("Mountain");
                revealedCard.setType(CardType.LAND);
                gd.playerHands.get(player2Id).add(revealedCard);
                CardView mockView = mock(CardView.class);
                when(cardViewFactory.create(revealedCard)).thenReturn(mockView);

                resolveEffect(gd, entry, new RevealRandomHandCardAndPlayEffect());

                verify(sessionManager).sendToPlayer(eq(player1Id), any(RevealHandMessage.class));
                verify(sessionManager).sendToPlayer(eq(player2Id), any(RevealHandMessage.class));
            }
}
