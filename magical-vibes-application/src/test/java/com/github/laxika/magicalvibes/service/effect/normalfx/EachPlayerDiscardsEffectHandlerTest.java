package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
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

                // Active player starts the discard; the amount rides the follow-up
                verify(playerInputService).beginDiscardChoice(eq(gd), eq(player1Id), anyInt(),
                        argThat((DiscardFollowUp f) -> f.eachPlayerAmount() == 1));
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

                // player2 should be in the carried queue remainder
                verify(playerInputService).beginDiscardChoice(eq(gd), eq(player1Id), anyInt(),
                        argThat((DiscardFollowUp f) -> f.remainingEachPlayerDiscards().equals(List.of(player2Id))));
            }

            @Test
            @DisplayName("Stores controller ID for opponent detection")
            void storesControllerId() {
                Card card = createCard("Syphon Mind");
                EachPlayerDiscardsEffect effect = new EachPlayerDiscardsEffect(1);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));
                gd.playerHands.get(player1Id).add(createCard("A"));

                resolveEffect(gd, entry, effect);

                verify(playerInputService).beginDiscardChoice(eq(gd), any(), anyInt(),
                        argThat((DiscardFollowUp f) -> player1Id.equals(f.eachPlayerControllerId())));
            }


    @Test
            @DisplayName("Begins discard for next player in queue")
            void beginsDiscardForNextPlayer() {
                gd.playerHands.get(player2Id).add(createCard("Mountain"));

                support.startNextEachPlayerDiscard(gd,
                        DiscardFollowUp.eachPlayer(List.of(player2Id), player1Id, 1));

                verify(playerInputService).beginDiscardChoice(eq(gd), eq(player2Id), anyInt(),
                        any(DiscardFollowUp.class));
                assertThat(gd.discardCausedByOpponent).isTrue();
            }

            @Test
            @DisplayName("Skips players with empty hands")
            void skipsPlayersWithEmptyHands() {
                // player1 has empty hand, player2 has cards
                gd.playerHands.get(player2Id).add(createCard("Mountain"));

                support.startNextEachPlayerDiscard(gd,
                        DiscardFollowUp.eachPlayer(List.of(player1Id, player2Id), player1Id, 1));

                // player1 skipped, player2 gets the discard prompt
                verify(playerInputService).beginDiscardChoice(eq(gd), eq(player2Id), anyInt(),
                        any(DiscardFollowUp.class));
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("Player1") && msg.contains("no cards to discard")));
            }

            @Test
            @DisplayName("Begins nothing when all players are done")
            void beginsNothingWhenDone() {
                // Empty queue — all players already processed

                support.startNextEachPlayerDiscard(gd,
                        DiscardFollowUp.eachPlayer(List.of(), player1Id, 1));

                verify(playerInputService, never()).beginDiscardChoice(any(), any(), anyInt(),
                        any(DiscardFollowUp.class));
            }

            @Test
            @DisplayName("Controller's own discard is not marked as opponent-caused")
            void controllerDiscardNotOpponentCaused() {
                gd.playerHands.get(player1Id).add(createCard("Mountain"));

                support.startNextEachPlayerDiscard(gd,
                        DiscardFollowUp.eachPlayer(List.of(player1Id), player1Id, 1));

                assertThat(gd.discardCausedByOpponent).isFalse();
            }
}
