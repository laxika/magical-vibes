package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DiscardHandEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
    @DisplayName("Discards all cards from hand to graveyard")
    void discardsAllCards() {
        Card card = createCard("One with Nothing");
        Card handCard1 = createCard("Mountain");
        Card handCard2 = createCard("Forest");
        gd.playerHands.get(player1Id).addAll(List.of(handCard1, handCard2));
        StackEntry entry = createEntry(card, player1Id, List.of(new DiscardHandEffect()));

        resolveEffect(gd, entry, new DiscardHandEffect());

        assertThat(gd.playerHands.get(player1Id)).isEmpty();
        verify(graveyardService).discardCard(gd, player1Id, handCard1);
        verify(graveyardService).discardCard(gd, player1Id, handCard2);
        verify(triggerCollectionService).checkDiscardTriggers(gd, player1Id, handCard1);
        verify(triggerCollectionService).checkDiscardTriggers(gd, player1Id, handCard2);
    }

    @Test
    @DisplayName("Returns the number of cards discarded")
    void returnsDiscardCount() {
        Card handCard1 = createCard("Mountain");
        Card handCard2 = createCard("Forest");
        gd.playerHands.get(player1Id).addAll(List.of(handCard1, handCard2));

        int discardCount = new DiscardHandEffectHandler(gameLogService, graveyardService, triggerCollectionService)
                .discardHand(gd, player1Id, player1Id, "One with Nothing");

        assertThat(discardCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Does nothing with empty hand")
    void doesNothingWithEmptyHand() {
        Card card = createCard("One with Nothing");
        StackEntry entry = createEntry(card, player1Id, List.of(new DiscardHandEffect()));

        resolveEffect(gd, entry, new DiscardHandEffect());

        verify(graveyardService, never()).discardCard(any(), any(), any());
        verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) ->
                logEntry.plainText().contains("no cards to discard")));
    }

    @Test
    @DisplayName("Sets discardCausedByOpponent to false")
    void setsDiscardCausedByOpponent() {
        Card card = createCard("One with Nothing");
        gd.playerHands.get(player1Id).add(createCard("Mountain"));
        gd.discardCausedByOpponent = true;
        StackEntry entry = createEntry(card, player1Id, List.of(new DiscardHandEffect()));

        resolveEffect(gd, entry, new DiscardHandEffect());

        assertThat(gd.discardCausedByOpponent).isFalse();
    }

    @Test
    @DisplayName("Logs discard count correctly")
    void logsDiscardCount() {
        Card card = createCard("One with Nothing");
        gd.playerHands.get(player1Id).addAll(List.of(createCard("A"), createCard("B"), createCard("C")));
        StackEntry entry = createEntry(card, player1Id, List.of(new DiscardHandEffect()));

        resolveEffect(gd, entry, new DiscardHandEffect());

        verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) ->
                logEntry.plainText().contains("discards their hand") && logEntry.plainText().contains("3 cards")));
    }

    @Test
    @DisplayName("Discards every player in a target group")
    void discardsEveryTargetPlayer() {
        Card card = createCard("Wheel and Deal");
        DiscardHandEffect effect = new DiscardHandEffect(DiscardRecipient.TARGET_PLAYER);
        card.target(0, 2).addEffect(EffectSlot.SPELL, effect);
        Card player1Card = createCard("Mountain");
        Card player2Card = createCard("Forest");
        gd.playerHands.get(player1Id).add(player1Card);
        gd.playerHands.get(player2Id).add(player2Card);
        StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, card, player1Id,
                card.getName(), List.of(effect), 0, List.of(player1Id, player2Id));

        resolveEffect(gd, entry, effect);

        assertThat(gd.playerHands.get(player1Id)).isEmpty();
        assertThat(gd.playerHands.get(player2Id)).isEmpty();
        verify(graveyardService).discardCard(gd, player1Id, player1Card);
        verify(graveyardService).discardCard(gd, player2Id, player2Card);
    }
}
