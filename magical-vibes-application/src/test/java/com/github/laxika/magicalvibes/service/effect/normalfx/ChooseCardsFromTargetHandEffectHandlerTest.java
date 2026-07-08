package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ChooseCardsFromTargetHandEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Nested
    @DisplayName("DISCARD destination")
    class Discard {

        @Test
        @DisplayName("Reveals hand, sets discardCausedByOpponent, and begins choice when target has valid cards")
        void revealsHandAndBeginsChoice() {
            Card card = createCard("Thoughtseize");
            var effect = new ChooseCardsFromTargetHandEffect(1, List.of(CardType.LAND), HandChoiceDestination.DISCARD);
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);
            Card targetCard = createCard("Lightning Bolt");
            targetCard.setType(CardType.INSTANT);
            gd.playerHands.get(player2Id).add(targetCard);

            resolveEffect(gd, entry, effect);

            assertThat(gd.discardCausedByOpponent).isTrue();
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("reveals their hand")));
            verify(interactionHandlerRegistry).begin(eq(gd), argThat(i ->
                    i instanceof PendingInteraction.RevealedHandChoice rhc
                            && rhc.choosingPlayerId().equals(player1Id)
                            && rhc.targetPlayerId().equals(player2Id)
                            && rhc.discardMode()));
        }

        @Test
        @DisplayName("Logs empty hand when target has no cards")
        void emptyHand() {
            Card card = createCard("Thoughtseize");
            var effect = new ChooseCardsFromTargetHandEffect(1, List.of(CardType.LAND), HandChoiceDestination.DISCARD);
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

            resolveEffect(gd, entry, effect);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("hand") && msg.contains("empty")));
        }
    }

    @Nested
    @DisplayName("EXILE destination")
    class Exile {

        @Test
        @DisplayName("Reveals hand and begins exile choice")
        void revealsHandAndBeginsExile() {
            Card card = createCard("Tidehollow Sculler");
            var effect = new ChooseCardsFromTargetHandEffect(1, List.of(CardType.LAND), HandChoiceDestination.EXILE);
            UUID sourcePermanentId = UUID.randomUUID();
            StackEntry entry = createTriggeredEntryWithTarget(card, player1Id, List.of(effect), player2Id, sourcePermanentId);
            Card targetCard = createCard("Lightning Bolt");
            targetCard.setType(CardType.INSTANT);
            gd.playerHands.get(player2Id).add(targetCard);

            resolveEffect(gd, entry, effect);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("reveals their hand")));
            verify(interactionHandlerRegistry).begin(eq(gd), argThat(i ->
                    i instanceof PendingInteraction.RevealedHandChoice rhc
                            && rhc.choosingPlayerId().equals(player1Id)
                            && rhc.targetPlayerId().equals(player2Id)
                            && rhc.exileMode()));
        }

        @Test
        @DisplayName("Forwards source permanent id when returnOnSourceLeave")
        void forwardsSourcePermanentIdForReturnOnLeave() {
            Card card = createCard("Kitesail Freebooter");
            var effect = new ChooseCardsFromTargetHandEffect(1, List.of(CardType.CREATURE, CardType.LAND),
                    List.of(), HandChoiceDestination.EXILE, true);
            UUID sourcePermanentId = UUID.randomUUID();
            StackEntry entry = createTriggeredEntryWithTarget(card, player1Id, List.of(effect), player2Id, sourcePermanentId);
            Card targetCard = createCard("Lightning Bolt");
            targetCard.setType(CardType.INSTANT);
            gd.playerHands.get(player2Id).add(targetCard);

            resolveEffect(gd, entry, effect);

            verify(interactionHandlerRegistry).begin(eq(gd), argThat(i ->
                    i instanceof PendingInteraction.RevealedHandChoice rhc
                            && rhc.exileMode()
                            && sourcePermanentId.equals(rhc.sourcePermanentId())));
        }

        @Test
        @DisplayName("Logs empty hand when target has no cards")
        void emptyHand() {
            Card card = createCard("Tidehollow Sculler");
            var effect = new ChooseCardsFromTargetHandEffect(1, List.of(CardType.LAND), HandChoiceDestination.EXILE);
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

            resolveEffect(gd, entry, effect);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("hand") && msg.contains("empty")));
        }
    }

    @Nested
    @DisplayName("TOP_OF_LIBRARY destination")
    class TopOfLibrary {

        @Test
        @DisplayName("Reveals hand and begins choice when target has cards")
        void revealsAndBeginsChoice() {
            Card card = createCard("Lapse of Certainty");
            var effect = new ChooseCardsFromTargetHandEffect(1, List.of(), HandChoiceDestination.TOP_OF_LIBRARY);
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);
            gd.playerHands.get(player2Id).addAll(List.of(createCard("A"), createCard("B")));

            resolveEffect(gd, entry, effect);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("looks at") && msg.contains("Player2")));
            verify(interactionHandlerRegistry).begin(eq(gd), argThat(i ->
                    i instanceof PendingInteraction.RevealedHandChoice rhc
                            && rhc.choosingPlayerId().equals(player1Id)
                            && rhc.targetPlayerId().equals(player2Id)
                            && !rhc.discardMode()
                            && !rhc.exileMode()));
        }

        @Test
        @DisplayName("Logs empty hand when target has no cards")
        void emptyHand() {
            Card card = createCard("Lapse of Certainty");
            var effect = new ChooseCardsFromTargetHandEffect(1, List.of(), HandChoiceDestination.TOP_OF_LIBRARY);
            StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

            resolveEffect(gd, entry, effect);

            verify(interactionHandlerRegistry, never()).begin(any(), any());
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("empty")));
        }
    }
}
