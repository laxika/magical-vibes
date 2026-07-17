package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DiscardEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Nested
    @DisplayName("CONTROLLER (chosen)")
    class Controller {

        @Test
        @DisplayName("Sets discardCausedByOpponent to false and begins discard")
        void setsDiscardFlag() {
            Card card = createCard("Sift");
            DiscardEffect effect = new DiscardEffect(1, DiscardRecipient.CONTROLLER);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));
            gd.playerHands.get(player1Id).add(createCard("Mountain"));

            resolveEffect(gd, entry, effect);

            assertThat(gd.discardCausedByOpponent).isFalse();
            verify(playerInputService).beginDiscardChoice(eq(gd), eq(player1Id), eq(1),
                    any(DiscardFollowUp.class));
        }

        @Test
        @DisplayName("Logs message when hand is empty")
        void logsWhenHandEmpty() {
            Card card = createCard("Sift");
            DiscardEffect effect = new DiscardEffect(1, DiscardRecipient.CONTROLLER);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));

            resolveEffect(gd, entry, effect);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) ->
                    logEntry.plainText().contains("no cards to discard")));
            verify(playerInputService, never()).beginDiscardChoice(any(), any(), anyInt(),
                    any(DiscardFollowUp.class));
        }
    }

    @Nested
    @DisplayName("TARGET_PLAYER (chosen)")
    class TargetPlayer {

        @Test
        @DisplayName("Target player discards with opponent flag set")
        void targetPlayerDiscardsWithOpponentFlag() {
            Card card = createCard("Mind Rot");
            DiscardEffect effect = new DiscardEffect(2, DiscardRecipient.TARGET_PLAYER);
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);
            gd.playerHands.get(player2Id).addAll(List.of(createCard("A"), createCard("B")));

            resolveEffect(gd, entry, effect);

            assertThat(gd.discardCausedByOpponent).isTrue();
            verify(playerInputService).beginDiscardChoice(eq(gd), eq(player2Id), eq(2),
                    any(DiscardFollowUp.class));
        }

        @Test
        @DisplayName("Logs when target has empty hand")
        void logsWhenTargetHandEmpty() {
            Card card = createCard("Mind Rot");
            DiscardEffect effect = new DiscardEffect(2, DiscardRecipient.TARGET_PLAYER);
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

            resolveEffect(gd, entry, effect);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) ->
                    logEntry.plainText().contains("no cards to discard")));
        }

        @Test
        @DisplayName("Logs discards-0 and skips prompt when amount evaluates to 0")
        void logsZeroWhenAmountZero() {
            Card card = createCard("Shrine of Limitless Power");
            // XValue with no cast-time X => evaluates to 0
            DiscardEffect effect = new DiscardEffect(new XValue(), DiscardRecipient.TARGET_PLAYER);
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);
            gd.playerHands.get(player2Id).add(createCard("A"));

            resolveEffect(gd, entry, effect);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) ->
                    logEntry.plainText().contains("discards 0 cards")));
            verify(playerInputService, never()).beginDiscardChoice(any(), any(), anyInt(),
                    any(DiscardFollowUp.class));
        }
    }

    @Nested
    @DisplayName("EACH_PLAYER (chosen)")
    class EachPlayer {

        @Test
        @DisplayName("APNAP order: active player is first, amount rides the follow-up")
        void activePlayerFirst() {
            Card card = createCard("Liliana of the Veil");
            DiscardEffect effect = new DiscardEffect(1, DiscardRecipient.EACH_PLAYER);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));
            gd.activePlayerId = player1Id;
            gd.playerHands.get(player1Id).add(createCard("A"));

            resolveEffect(gd, entry, effect);

            verify(playerInputService).beginDiscardChoice(eq(gd), eq(player1Id), anyInt(),
                    argThat((DiscardFollowUp f) -> f.eachPlayerAmount() == 1));
        }

        @Test
        @DisplayName("Opponent is in the carried queue remainder, controllerId stored")
        void opponentInQueueAfterActivePlayer() {
            Card card = createCard("Liliana of the Veil");
            DiscardEffect effect = new DiscardEffect(1, DiscardRecipient.EACH_PLAYER);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));
            gd.activePlayerId = player1Id;
            gd.playerHands.get(player1Id).add(createCard("A"));
            gd.playerHands.get(player2Id).add(createCard("B"));

            resolveEffect(gd, entry, effect);

            verify(playerInputService).beginDiscardChoice(eq(gd), eq(player1Id), anyInt(),
                    argThat((DiscardFollowUp f) -> f.remainingEachPlayerDiscards().equals(List.of(player2Id))
                            && player1Id.equals(f.eachPlayerControllerId())));
        }
    }

    @Nested
    @DisplayName("EACH_OPPONENT (chosen)")
    class EachOpponent {

        @Test
        @DisplayName("Only opponents discard; active opponent is first")
        void onlyOpponentsDiscard() {
            Card card = createCard("Hymn to Tourach");
            DiscardEffect effect = new DiscardEffect(2, DiscardRecipient.EACH_OPPONENT);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));
            gd.activePlayerId = player2Id;
            gd.playerHands.get(player2Id).addAll(List.of(createCard("A"), createCard("B")));

            resolveEffect(gd, entry, effect);

            verify(playerInputService).beginDiscardChoice(eq(gd), eq(player2Id), anyInt(),
                    argThat((DiscardFollowUp f) -> f.eachPlayerAmount() == 2));
        }

        @Test
        @DisplayName("Controller is excluded from the queue")
        void controllerExcludedFromQueue() {
            Card card = createCard("Hymn to Tourach");
            DiscardEffect effect = new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));
            gd.activePlayerId = player1Id;
            gd.playerHands.get(player2Id).add(createCard("A"));

            resolveEffect(gd, entry, effect);

            // Active player is the controller (skipped); player2 is the only opponent and starts.
            verify(playerInputService).beginDiscardChoice(eq(gd), eq(player2Id), anyInt(),
                    any(DiscardFollowUp.class));
        }
    }

    @Nested
    @DisplayName("startNextEachPlayerDiscard (queue advance)")
    class QueueAdvance {

        @Test
        @DisplayName("Begins discard for next player and marks opponent-caused")
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
            gd.playerHands.get(player2Id).add(createCard("Mountain"));

            support.startNextEachPlayerDiscard(gd,
                    DiscardFollowUp.eachPlayer(List.of(player1Id, player2Id), player1Id, 1));

            verify(playerInputService).beginDiscardChoice(eq(gd), eq(player2Id), anyInt(),
                    any(DiscardFollowUp.class));
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) ->
                    logEntry.plainText().contains("Player1") && logEntry.plainText().contains("no cards to discard")));
        }

        @Test
        @DisplayName("Begins nothing when the queue is empty")
        void beginsNothingWhenDone() {
            support.startNextEachPlayerDiscard(gd,
                    DiscardFollowUp.eachPlayer(List.of(), player1Id, 1));

            verify(playerInputService, never()).beginDiscardChoice(any(), any(), anyInt(),
                    any(DiscardFollowUp.class));
        }

        @Test
        @DisplayName("Controller's own discard is not opponent-caused")
        void controllerDiscardNotOpponentCaused() {
            gd.playerHands.get(player1Id).add(createCard("Mountain"));

            support.startNextEachPlayerDiscard(gd,
                    DiscardFollowUp.eachPlayer(List.of(player1Id), player1Id, 1));

            assertThat(gd.discardCausedByOpponent).isFalse();
        }
    }

    @Nested
    @DisplayName("Random discard")
    class Random {

        @Test
        @DisplayName("Target player discards at random with opponent flag")
        void targetDiscardsAtRandom() {
            Card card = createCard("Hypnotic Specter");
            DiscardEffect effect = new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER, true);
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);
            gd.playerHands.get(player2Id).add(createCard("Mountain"));

            resolveEffect(gd, entry, effect);

            assertThat(gd.discardCausedByOpponent).isTrue();
            assertThat(gd.playerHands.get(player2Id)).isEmpty();
            verify(graveyardService).discardCard(eq(gd), eq(player2Id), any());
        }

        @Test
        @DisplayName("Controller discards at random for a self-discard (CONTROLLER)")
        void controllerDiscardsAtRandom() {
            Card card = createCard("Goblin Lore");
            DiscardEffect effect = new DiscardEffect(1, DiscardRecipient.CONTROLLER, true);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));
            gd.playerHands.get(player1Id).add(createCard("Mountain"));

            resolveEffect(gd, entry, effect);

            assertThat(gd.discardCausedByOpponent).isFalse();
            assertThat(gd.playerHands.get(player1Id)).isEmpty();
        }

        @Test
        @DisplayName("No random discard when hand is empty")
        void noDiscardWhenHandEmpty() {
            Card card = createCard("Hypnotic Specter");
            DiscardEffect effect = new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER, true);
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

            resolveEffect(gd, entry, effect);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) ->
                    logEntry.plainText().contains("no cards to discard")));
        }

        @Test
        @DisplayName("Each player discards at random in APNAP order")
        void eachPlayerDiscardsAPNAP() {
            Card card = createCard("Burning Inquiry");
            DiscardEffect effect = new DiscardEffect(1, DiscardRecipient.EACH_PLAYER, true);
            StackEntry entry = createEntry(card, player1Id, List.of(effect));
            gd.activePlayerId = player1Id;
            gd.playerHands.get(player1Id).add(createCard("Mountain"));
            gd.playerHands.get(player2Id).add(createCard("Forest"));

            resolveEffect(gd, entry, effect);

            assertThat(gd.playerHands.get(player1Id)).isEmpty();
            assertThat(gd.playerHands.get(player2Id)).isEmpty();
            verify(graveyardService, times(2)).discardCard(eq(gd), any(), any());
        }
    }
}
