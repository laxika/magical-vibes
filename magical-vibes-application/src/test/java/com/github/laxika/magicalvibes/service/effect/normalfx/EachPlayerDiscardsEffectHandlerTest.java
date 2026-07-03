package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EachPlayerDiscardsEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("APNAP order: active player is first in the queue")
            void activePlayerFirst() {
                Card card = createCard("Syphon Mind");
                EachPlayerDiscardsEffect effect = new EachPlayerDiscardsEffect(1);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));
                gd.activePlayerId = player1Id;
                gd.playerHands.get(player1Id).add(createCard("A"));

                resolveEffect(gd, entry, effect);

                // Active player starts the discard
                verify(playerInputService).beginDiscardChoice(eq(gd), eq(player1Id), anyInt());
                assertThat(gd.pendingEachPlayerDiscardAmount).isEqualTo(1);
            }

            @Test
            @DisplayName("Opponent is in queue after active player")
            void opponentInQueueAfterActivePlayer() {
                Card card = createCard("Syphon Mind");
                EachPlayerDiscardsEffect effect = new EachPlayerDiscardsEffect(1);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));
                gd.activePlayerId = player1Id;
                gd.playerHands.get(player1Id).add(createCard("A"));
                gd.playerHands.get(player2Id).add(createCard("B"));

                resolveEffect(gd, entry, effect);

                // player2 should be in the pending queue
                assertThat(gd.pendingEachPlayerDiscardQueue).containsExactly(player2Id);
            }

            @Test
            @DisplayName("Stores controller ID for opponent detection")
            void storesControllerId() {
                Card card = createCard("Syphon Mind");
                EachPlayerDiscardsEffect effect = new EachPlayerDiscardsEffect(1);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));
                gd.playerHands.get(player1Id).add(createCard("A"));

                resolveEffect(gd, entry, effect);

                assertThat(gd.pendingEachPlayerDiscardControllerId).isEqualTo(player1Id);
            }


    @Test
            @DisplayName("Begins discard for next player in queue")
            void beginsDiscardForNextPlayer() {
                gd.pendingEachPlayerDiscardQueue.add(player2Id);
                gd.pendingEachPlayerDiscardAmount = 1;
                gd.pendingEachPlayerDiscardControllerId = player1Id;
                gd.playerHands.get(player2Id).add(createCard("Mountain"));

                support.startNextEachPlayerDiscard(gd);

                verify(playerInputService).beginDiscardChoice(eq(gd), eq(player2Id), anyInt());
                assertThat(gd.discardCausedByOpponent).isTrue();
            }

            @Test
            @DisplayName("Skips players with empty hands")
            void skipsPlayersWithEmptyHands() {
                gd.pendingEachPlayerDiscardQueue.add(player1Id);
                gd.pendingEachPlayerDiscardQueue.add(player2Id);
                gd.pendingEachPlayerDiscardAmount = 1;
                gd.pendingEachPlayerDiscardControllerId = player1Id;
                // player1 has empty hand, player2 has cards
                gd.playerHands.get(player2Id).add(createCard("Mountain"));

                support.startNextEachPlayerDiscard(gd);

                // player1 skipped, player2 gets the discard prompt
                verify(playerInputService).beginDiscardChoice(eq(gd), eq(player2Id), anyInt());
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("Player1") && msg.contains("no cards to discard")));
            }

            @Test
            @DisplayName("Clears controller tracking when all players done")
            void clearsControllerWhenDone() {
                gd.pendingEachPlayerDiscardControllerId = player1Id;
                // Empty queue â€” all players already processed

                support.startNextEachPlayerDiscard(gd);

                assertThat(gd.pendingEachPlayerDiscardControllerId).isNull();
                verify(playerInputService, never()).beginDiscardChoice(any(), any(), anyInt());
            }

            @Test
            @DisplayName("Controller's own discard is not marked as opponent-caused")
            void controllerDiscardNotOpponentCaused() {
                gd.pendingEachPlayerDiscardQueue.add(player1Id);
                gd.pendingEachPlayerDiscardAmount = 1;
                gd.pendingEachPlayerDiscardControllerId = player1Id;
                gd.playerHands.get(player1Id).add(createCard("Mountain"));

                support.startNextEachPlayerDiscard(gd);

                assertThat(gd.discardCausedByOpponent).isFalse();
            }
}
