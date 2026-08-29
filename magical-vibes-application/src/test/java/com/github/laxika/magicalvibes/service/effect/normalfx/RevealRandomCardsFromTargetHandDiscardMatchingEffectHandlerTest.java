package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.RevealRandomCardsFromTargetHandDiscardMatchingEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RevealRandomCardsFromTargetHandDiscardMatchingEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
    void revealsTwoRandomCardsAndDiscardsMatchingCards() {
        Card source = createCard("Rise // Fall");
        Card land = createCard("Forest");
        land.setType(CardType.LAND);
        Card creature = createCard("Grizzly Bears");
        creature.setType(CardType.CREATURE);
        gd.playerHands.get(player2Id).addAll(List.of(land, creature));
        var predicate = new CardTypePredicate(CardType.CREATURE);
        when(predicateEvaluationService.matchesCardPredicate(
                eq(creature), eq(predicate), any(), eq(gd), eq(player2Id))).thenReturn(true);
        when(predicateEvaluationService.matchesCardPredicate(
                eq(land), eq(predicate), any(), eq(gd), eq(player2Id))).thenReturn(false);

        StackEntry entry = createEntryWithTarget(source, player1Id,
                List.of(new RevealRandomCardsFromTargetHandDiscardMatchingEffect(2, predicate)), player2Id);
        resolveEffect(gd, entry, new RevealRandomCardsFromTargetHandDiscardMatchingEffect(2, predicate));

        verify(cardRevealService).revealToAllPlayers(eq(gd), eq(player2Id), any(),
                org.mockito.ArgumentMatchers.argThat(cards -> cards.containsAll(List.of(land, creature))));
        verify(graveyardService).discardCard(gd, player2Id, creature);
        assertThat(gd.playerHands.get(player2Id)).containsExactly(land);
    }
}
