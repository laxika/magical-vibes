package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.github.laxika.magicalvibes.model.effect.Subjects;

class DiscardEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Nested
    @DisplayName("CONTROLLER (chosen)")
    class Controller {

        @Test
        @DisplayName("Sets discardCausedByOpponent to false and begins discard")
        void setsDiscardFlag() {
            Card card = createCard("Sift");
            DiscardEffect effect = new DiscardEffect(1, Subjects.you());
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
            DiscardEffect effect = new DiscardEffect(1, Subjects.you());
            StackEntry entry = createEntry(card, player1Id, List.of(effect));

            resolveEffect(gd, entry, effect);

            verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) ->
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
            DiscardEffect effect = new DiscardEffect(2, Subjects.targetPlayer());
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
            DiscardEffect effect = new DiscardEffect(2, Subjects.targetPlayer());
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

            resolveEffect(gd, entry, effect);

            verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) ->
                    logEntry.plainText().contains("no cards to discard")));
        }

        @Test
        @DisplayName("Logs discards-0 and skips prompt when amount evaluates to 0")
        void logsZeroWhenAmountZero() {
            Card card = createCard("Shrine of Limitless Power");
            // XValue with no cast-time X => evaluates to 0
            DiscardEffect effect = new DiscardEffect(new XValue(), Subjects.targetPlayer());
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);
            gd.playerHands.get(player2Id).add(createCard("A"));

            resolveEffect(gd, entry, effect);

            verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) ->
                    logEntry.plainText().contains("discards 0 cards")));
            verify(playerInputService, never()).beginDiscardChoice(any(), any(), anyInt(),
                    any(DiscardFollowUp.class));
        }
    }

    @Nested
    @DisplayName("TRIGGERING_PLAYER (chosen)")
    class TriggeringPlayer {

        @Test
        @DisplayName("Triggering player discards without a target and tracks opponent cause")
        void triggeringPlayerDiscards() {
            Card card = createCard("Putrefaction");
            DiscardEffect effect = new DiscardEffect(1, Subjects.triggeringPlayer());
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);
            gd.playerHands.get(player2Id).add(createCard("A"));

            resolveEffect(gd, entry, effect);

            assertThat(effect.targetSpec()).isEqualTo(com.github.laxika.magicalvibes.model.effect.TargetSpec.NONE);
            assertThat(gd.discardCausedByOpponent).isTrue();
            verify(playerInputService).beginDiscardChoice(eq(gd), eq(player2Id), eq(1),
                    any(DiscardFollowUp.class));
        }

        @Test
        @DisplayName("Controller casting their own spell is not opponent-caused")
        void controllerIsNotOpponentCause() {
            Card card = createCard("Putrefaction");
            DiscardEffect effect = new DiscardEffect(1, Subjects.triggeringPlayer());
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player1Id);
            gd.playerHands.get(player1Id).add(createCard("A"));

            resolveEffect(gd, entry, effect);

            assertThat(gd.discardCausedByOpponent).isFalse();
        }
    }

    @Nested
    @DisplayName("EACH_PLAYER (chosen)")
    class EachPlayer {

        @Test
        @DisplayName("APNAP order: the active player chooses first, for the effect's amount")
        void activePlayerFirst() {
            Card card = createCard("Liliana of the Veil");
            DiscardEffect effect = new DiscardEffect(1, Subjects.eachPlayer());
            StackEntry entry = createEntry(card, player1Id, List.of(effect));
            gd.activePlayerId = player1Id;
            gd.playerHands.get(player1Id).add(createCard("A"));

            resolveEffect(gd, entry, effect);

            verify(playerInputService).beginDiscardChoice(eq(gd), eq(player1Id), anyList(), anyString(), eq(1),
                    any(DiscardFollowUp.class));
            assertThat(gd.eachPlayerRummage.currentPlayerId).isEqualTo(player1Id);
        }

        @Test
        @DisplayName("Opponent waits in the queue after the active player, and nothing is discarded yet")
        void opponentInQueueAfterActivePlayer() {
            Card card = createCard("Liliana of the Veil");
            DiscardEffect effect = new DiscardEffect(1, Subjects.eachPlayer());
            StackEntry entry = createEntry(card, player1Id, List.of(effect));
            gd.activePlayerId = player1Id;
            gd.playerHands.get(player1Id).add(createCard("A"));
            gd.playerHands.get(player2Id).add(createCard("B"));

            resolveEffect(gd, entry, effect);

            verify(playerInputService).beginDiscardChoice(eq(gd), eq(player1Id), anyList(), anyString(), eq(1),
                    any(DiscardFollowUp.class));
            assertThat(gd.eachPlayerRummage.remaining).containsExactly(player2Id);
            assertThat(gd.eachPlayerRummage.deferDiscards).isTrue();
            assertThat(gd.playerHands.get(player1Id)).hasSize(1);
            assertThat(gd.playerHands.get(player2Id)).hasSize(1);
        }
    }

    @Nested
    @DisplayName("EACH_OPPONENT (chosen)")
    class EachOpponent {

        @Test
        @DisplayName("Only opponents discard; active opponent is first")
        void onlyOpponentsDiscard() {
            Card card = createCard("Hymn to Tourach");
            DiscardEffect effect = new DiscardEffect(2, Subjects.eachOpponent());
            StackEntry entry = createEntry(card, player1Id, List.of(effect));
            gd.activePlayerId = player2Id;
            gd.playerHands.get(player2Id).addAll(List.of(createCard("A"), createCard("B")));

            resolveEffect(gd, entry, effect);

            verify(playerInputService).beginDiscardChoice(eq(gd), eq(player2Id), anyList(), anyString(), eq(2),
                    any(DiscardFollowUp.class));
            assertThat(gd.eachPlayerRummage.remaining).isEmpty();
        }

        @Test
        @DisplayName("Controller is excluded from the queue")
        void controllerExcludedFromQueue() {
            Card card = createCard("Hymn to Tourach");
            DiscardEffect effect = new DiscardEffect(1, Subjects.eachOpponent());
            StackEntry entry = createEntry(card, player1Id, List.of(effect));
            gd.activePlayerId = player1Id;
            gd.playerHands.get(player2Id).add(createCard("A"));

            resolveEffect(gd, entry, effect);

            // Active player is the controller (skipped); player2 is the only opponent and starts.
            verify(playerInputService).beginDiscardChoice(eq(gd), eq(player2Id), anyList(), anyString(), eq(1),
                    any(DiscardFollowUp.class));
            assertThat(gd.eachPlayerRummage.remaining).isEmpty();
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
            verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) ->
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
            DiscardEffect effect = new DiscardEffect(1, Subjects.targetPlayer(), true);
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
            DiscardEffect effect = new DiscardEffect(1, Subjects.you(), true);
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
            DiscardEffect effect = new DiscardEffect(1, Subjects.targetPlayer(), true);
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

            resolveEffect(gd, entry, effect);

            verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) ->
                    logEntry.plainText().contains("no cards to discard")));
        }

        @Test
        @DisplayName("Each player discards at random in APNAP order")
        void eachPlayerDiscardsAPNAP() {
            Card card = createCard("Burning Inquiry");
            DiscardEffect effect = new DiscardEffect(1, Subjects.eachPlayer(), true);
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

    @Nested
    @DisplayName("controllerOf(targetSpell()) (chosen)")
    class TargetSpellController {

        @Test
        @DisplayName("Discards for the controller of the targeted spell")
        void discardsForSpellController() {
            Card sourceCard = createCard("Counterspell");
            Card targetSpellCard = createCard("Lightning Bolt");
            StackEntry targetSpell = new StackEntry(StackEntryType.INSTANT_SPELL, targetSpellCard,
                    player2Id, targetSpellCard.getName(), List.of());
            gd.stack.add(targetSpell);

            DiscardEffect effect = new DiscardEffect(1, Subjects.controllerOf(Subjects.targetSpell()));
            StackEntry entry = createEntryWithTarget(sourceCard, player1Id, List.of(effect), targetSpellCard.getId());
            gd.playerHands.get(player2Id).add(createCard("Mountain"));

            resolveEffect(gd, entry, effect);

            assertThat(gd.discardCausedByOpponent).isTrue();
            verify(playerInputService).beginDiscardChoice(eq(gd), eq(player2Id), anyInt(),
                    any(DiscardFollowUp.class));
        }

        @Test
        @DisplayName("Does nothing when the targeted spell is not on the stack")
        void doesNothingWhenTargetNotOnStack() {
            Card sourceCard = createCard("Counterspell");
            DiscardEffect effect = new DiscardEffect(1, Subjects.controllerOf(Subjects.targetSpell()));
            StackEntry entry = createEntryWithTarget(sourceCard, player1Id, List.of(effect), UUID.randomUUID());

            resolveEffect(gd, entry, effect);

            verify(playerInputService, never()).beginDiscardChoice(any(), any(), anyInt(), any(DiscardFollowUp.class));
        }

        @Test
        @DisplayName("Does nothing when the entry has no target")
        void doesNothingWhenTargetIdNull() {
            Card sourceCard = createCard("Counterspell");
            DiscardEffect effect = new DiscardEffect(1, Subjects.controllerOf(Subjects.targetSpell()));
            StackEntry entry = createEntry(sourceCard, player1Id, List.of(effect));

            resolveEffect(gd, entry, effect);

            verify(playerInputService, never()).beginDiscardChoice(any(), any(), anyInt(), any(DiscardFollowUp.class));
        }
    }
}
