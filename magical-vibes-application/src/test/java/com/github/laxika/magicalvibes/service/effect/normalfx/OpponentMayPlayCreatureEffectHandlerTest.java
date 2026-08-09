package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.OpponentMayPlayCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OpponentMayPlayCreatureEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
    @DisplayName("Logs when opponent has no creatures in hand")
    void logsWhenOpponentHasNoCreatures() {
        Card card = createCard("Hunted Wumpus");
        StackEntry entry = createEntry(card, player1Id, List.of());

        when(gameQueryService.getOpponentId(gd, player1Id)).thenReturn(player2Id);

        resolveEffect(gd, entry, new OpponentMayPlayCreatureEffect());

        verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) ->
                logEntry.plainText().contains("no creature cards in hand")));
    }

    @Test
    @DisplayName("Begins card choice when opponent has creatures")
    void beginsCardChoiceWhenCreaturesAvailable() {
        Card card = createCard("Hunted Wumpus");
        StackEntry entry = createEntry(card, player1Id, List.of());
        Card creatureCard = createCard("Grizzly Bears");
        creatureCard.setType(CardType.CREATURE);
        gd.playerHands.get(player2Id).add(creatureCard);

        when(gameQueryService.getOpponentId(gd, player1Id)).thenReturn(player2Id);

        resolveEffect(gd, entry, new OpponentMayPlayCreatureEffect());

        verify(playerInputService).beginCardChoice(eq(gd), eq(player2Id), any(), any());
    }

    @Test
    @DisplayName("Uses the configured predicate when filtering the opponent's hand")
    void filtersWithConfiguredPredicate() {
        Card source = createCard("Iwamori of the Open Fist");
        StackEntry entry = createEntry(source, player1Id, List.of());
        Card nonlegendaryCreature = createCard("Grizzly Bears");
        nonlegendaryCreature.setType(CardType.CREATURE);
        Card legendaryCreature = createCard("Guan Yu, Sainted Warrior");
        legendaryCreature.setType(CardType.CREATURE);
        legendaryCreature.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        gd.playerHands.get(player2Id).addAll(List.of(nonlegendaryCreature, legendaryCreature));

        when(gameQueryService.getOpponentId(gd, player1Id)).thenReturn(player2Id);
        when(predicateEvaluationService.matchesCardPredicate(any(Card.class), any(), any()))
                .thenAnswer(invocation -> invocation.getArgument(0) == legendaryCreature);

        resolveEffect(gd, entry, new OpponentMayPlayCreatureEffect(
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardSupertypePredicate(CardSupertype.LEGENDARY))),
                "legendary creature"));

        verify(playerInputService).beginCardChoice(eq(gd), eq(player2Id), eq(List.of(1)),
                eq("You may put a legendary creature card from your hand onto the battlefield."));
    }
}
