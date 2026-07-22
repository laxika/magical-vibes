package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DiscardCardThenEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
    @DisplayName("Begins filtered discard with thenEffect follow-up when matching cards exist")
    void beginsFilteredDiscardWithThenEffect() {
        Card card = createCard("Pack Guardian");
        Card land = createCard("Forest");
        Card creature = createCard("Bear");
        gd.playerHands.get(player1Id).add(land);
        gd.playerHands.get(player1Id).add(creature);

        CreateTokenEffect token = new CreateTokenEffect("Wolf", 2, 2,
                CardColor.GREEN, List.of(CardSubtype.WOLF), Set.of(), Set.of());
        DiscardCardThenEffect effect = new DiscardCardThenEffect(
                new CardTypePredicate(CardType.LAND), token, "a land card");
        StackEntry entry = createEntry(card, player1Id, List.of(effect));

        when(predicateEvaluationService.matchesCardPredicate(eq(land), any(), any()))
                .thenReturn(true);
        when(predicateEvaluationService.matchesCardPredicate(eq(creature), any(), any()))
                .thenReturn(false);

        resolveEffect(gd, entry, effect);

        assertThat(gd.discardCausedByOpponent).isFalse();
        verify(playerInputService).beginDiscardChoice(eq(gd), eq(player1Id),
                argThat((List<Integer> indices) -> indices.equals(List.of(0))),
                anyString(), eq(1),
                argThat((DiscardFollowUp f) -> f.thenEffect() == token
                        && f.thenEffectSourceCard() == card));
    }

    @Test
    @DisplayName("Logs and does nothing when no matching cards in hand")
    void noMatchingCards() {
        Card card = createCard("Pack Guardian");
        Card creature = createCard("Bear");
        gd.playerHands.get(player1Id).add(creature);

        DiscardCardThenEffect effect = new DiscardCardThenEffect(
                new CardTypePredicate(CardType.LAND),
                new CreateTokenEffect("Wolf", 2, 2,
                        CardColor.GREEN, List.of(CardSubtype.WOLF), Set.of(), Set.of()),
                "a land card");
        StackEntry entry = createEntry(card, player1Id, List.of(effect));

        when(predicateEvaluationService.matchesCardPredicate(eq(creature), any(), any()))
                .thenReturn(false);

        resolveEffect(gd, entry, effect);

        verify(playerInputService, never()).beginDiscardChoice(any(), any(), anyList(),
                anyString(), anyInt(), any());
        verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) ->
                logEntry.plainText().contains("no a land card to discard")));
    }
}
