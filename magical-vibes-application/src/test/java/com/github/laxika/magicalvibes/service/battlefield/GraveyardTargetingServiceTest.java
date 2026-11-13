package com.github.laxika.magicalvibes.service.battlefield;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;

@ExtendWith(MockitoExtension.class)
class GraveyardTargetingServiceTest {

    @Mock private GameQueryService gameQueryService;
    @Mock private PredicateEvaluationService predicateEvaluationService;
    @Mock private GameLogService gameLogService;
    @Mock private PlayerInputService playerInputService;

    private GraveyardTargetingService service;
    private GameData gd;
    private UUID player1Id;

    @BeforeEach
    void setUp() {
        service = new GraveyardTargetingService(predicateEvaluationService, gameLogService, playerInputService,
                gameQueryService);
        lenient().when(gameQueryService.canGraveyardCardsBeTargeted(any())).thenReturn(true);

        player1Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.orderedPlayerIds.add(player1Id);
        gd.playerBattlefields.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerGraveyards.put(player1Id, new ArrayList<>());
    }

    @Test
    @DisplayName("graveyard return targeting passes a total mana value cap to the choice")
    void handleReturnToBattlefieldTargeting_passesMaximumManaValue() {
        Card source = new Card();
        source.setName("Patch Up");
        Card creature = new Card();
        creature.setName("Creature");
        creature.setType(CardType.CREATURE);
        creature.setManaCost("{1}");
        Card expensiveCreature = new Card();
        expensiveCreature.setName("Expensive creature");
        expensiveCreature.setType(CardType.CREATURE);
        expensiveCreature.setManaCost("{4}");
        gd.playerGraveyards.get(player1Id).add(creature);
        gd.playerGraveyards.get(player1Id).add(expensiveCreature);
        ReturnTargetCardsFromGraveyardToBattlefieldEffect effect =
                new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                        new CardTypePredicate(CardType.CREATURE), 3, false, false,
                        null, 3, null, null, null, 0);
        when(predicateEvaluationService.matchesCardPredicate(
                eq(creature), eq(effect.filter()), eq(source.getId()), eq(gd), eq(player1Id),
                isNull(), isNull(), isNull())).thenReturn(true);
        service.handleUpToNGraveyardSpellTargeting(gd, player1Id, source,
                StackEntryType.SORCERY_SPELL, effect, List.of(effect));

        verify(playerInputService).beginMultiGraveyardChoiceWithMaximumManaValue(
                eq(gd), eq(player1Id), argThat(cards -> cards.contains(creature)
                        && !cards.contains(expensiveCreature)), eq(1), eq(0), eq(3), anyString());
    }

    @Test
    @DisplayName("graveyard return targeting accepts effects without a total mana value cap")
    void handleReturnToHandTargeting_acceptsNoMaximumManaValue() {
        Card source = new Card();
        source.setName("Pull from the Deep");
        Card instant = new Card();
        instant.setName("Instant");
        instant.setType(CardType.INSTANT);
        gd.playerGraveyards.get(player1Id).add(instant);
        ReturnTargetCardsFromGraveyardToHandEffect effect =
                new ReturnTargetCardsFromGraveyardToHandEffect(new CardTypePredicate(CardType.INSTANT), 1);
        when(predicateEvaluationService.matchesCardPredicate(
                eq(instant), eq(effect.filter()), eq(source.getId()), eq(gd), eq(player1Id),
                isNull(), isNull(), isNull())).thenReturn(true);

        service.handleUpToNGraveyardSpellTargeting(gd, player1Id, source,
                StackEntryType.SORCERY_SPELL, effect, 1, List.of(effect), 0);

        verify(playerInputService).beginMultiGraveyardChoice(
                eq(gd), eq(player1Id), argThat(cards -> cards.contains(instant)),
                eq(1), eq(0), anyString());
    }

}
