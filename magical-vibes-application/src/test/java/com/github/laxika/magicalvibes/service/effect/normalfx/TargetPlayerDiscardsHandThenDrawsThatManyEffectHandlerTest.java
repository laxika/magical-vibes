package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsHandThenDrawsThatManyEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class TargetPlayerDiscardsHandThenDrawsThatManyEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
    @DisplayName("Target discards all cards from hand then draws that many")
    void discardsAllThenDraws() {
        Card card = createCard("Collective Defiance");
        Card handCard1 = createCard("Mountain");
        Card handCard2 = createCard("Forest");
        gd.playerHands.get(player2Id).addAll(List.of(handCard1, handCard2));
        StackEntry entry = createEntryWithTarget(card, player1Id,
                List.of(new TargetPlayerDiscardsHandThenDrawsThatManyEffect()), player2Id);

        resolveEffect(gd, entry, new TargetPlayerDiscardsHandThenDrawsThatManyEffect());

        assertThat(gd.playerHands.get(player2Id)).isEmpty();
        assertThat(gd.discardCausedByOpponent).isTrue();
        verify(graveyardService).discardCard(gd, player2Id, handCard1);
        verify(graveyardService).discardCard(gd, player2Id, handCard2);
        verify(drawService, times(2)).resolveDrawCard(gd, player2Id);
        verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) ->
                logEntry.plainText().contains("draws 2 cards")));
    }

    @Test
    @DisplayName("Does nothing with empty hand")
    void doesNothingWithEmptyHand() {
        Card card = createCard("Collective Defiance");
        StackEntry entry = createEntryWithTarget(card, player1Id,
                List.of(new TargetPlayerDiscardsHandThenDrawsThatManyEffect()), player2Id);

        resolveEffect(gd, entry, new TargetPlayerDiscardsHandThenDrawsThatManyEffect());

        verify(graveyardService, never()).discardCard(any(), any(), any());
        verify(drawService, never()).resolveDrawCard(any(), any());
        verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) ->
                logEntry.plainText().contains("no cards to discard")));
    }

    @Test
    @DisplayName("Self-target is not opponent-caused discard")
    void selfTargetNotOpponentCaused() {
        Card card = createCard("Collective Defiance");
        Card handCard = createCard("Mountain");
        gd.playerHands.get(player1Id).add(handCard);
        StackEntry entry = createEntryWithTarget(card, player1Id,
                List.of(new TargetPlayerDiscardsHandThenDrawsThatManyEffect()), player1Id);

        resolveEffect(gd, entry, new TargetPlayerDiscardsHandThenDrawsThatManyEffect());

        assertThat(gd.discardCausedByOpponent).isFalse();
        verify(graveyardService).discardCard(gd, player1Id, handCard);
        verify(drawService).resolveDrawCard(gd, player1Id);
    }
}
