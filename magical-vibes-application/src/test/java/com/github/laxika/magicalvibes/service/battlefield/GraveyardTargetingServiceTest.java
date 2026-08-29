package com.github.laxika.magicalvibes.service.battlefield;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.GraveyardTargetingSupport;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
                gameQueryService, new GraveyardTargetingSupport());
        lenient().when(gameQueryService.canGraveyardCardsBeTargeted(any())).thenReturn(true);

        player1Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.orderedPlayerIds.add(player1Id);
        gd.playerBattlefields.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerGraveyards.put(player1Id, new ArrayList<>());
    }

    @Test
    @DisplayName("handleGraveyardExileETBTargeting pushes stack entry with no targets when all graveyards are empty")
    void handleGraveyardExileETBTargeting_pushesEmptyTargetEntryWhenGraveyardsEmpty() {
        Card card = new Card();
        card.setName("Agent of Treachery");
        ExileCardsFromGraveyardEffect exile = new ExileCardsFromGraveyardEffect(3, 0);
        List<CardEffect> allEffects = List.of(exile);

        service.handleGraveyardExileETBTargeting(gd, player1Id, card, allEffects, exile);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(card);
        verify(gameLogService).append(eq(gd), argThat((GameLogEntry e) -> e.plainText().equals("Agent of Treachery's enter-the-battlefield ability triggers.")));
    }

    @Test
    @DisplayName("handleGraveyardExileETBTargeting preserves the single-graveyard restriction")
    void handleGraveyardExileETBTargeting_preservesSingleGraveyardRestriction() {
        UUID player2Id = UUID.randomUUID();
        gd.orderedPlayerIds.add(player2Id);
        gd.playerGraveyards.put(player2Id, new ArrayList<>());
        gd.playerGraveyards.get(player1Id).add(new Card());
        gd.playerGraveyards.get(player2Id).add(new Card());

        Card card = new Card();
        card.setName("Soul-Shackled Zombie");
        ExileCardsFromGraveyardEffect exile = new ExileCardsFromGraveyardEffect(
                2, new CardTypePredicate(CardType.CREATURE), 2, 2, true);

        service.handleGraveyardExileETBTargeting(gd, player1Id, card, List.of(exile), exile);

        assertThat(gd.graveyardTargetOperation.singleGraveyard).isTrue();
        verify(playerInputService).beginMultiGraveyardChoice(eq(gd), eq(player1Id), any(), eq(2), anyString());
    }

    @Test
    @DisplayName("handleReturnToHandETBTargeting pushes empty-target trigger when no matching land cards")
    void handleReturnToHandETBTargeting_pushesEmptyTriggerWhenNoLands() {
        Card card = new Card();
        card.setName("Tilling Treefolk");
        ReturnTargetCardsFromGraveyardToHandEffect effect =
                new ReturnTargetCardsFromGraveyardToHandEffect(new CardTypePredicate(CardType.LAND), 2);

        service.handleReturnToHandETBTargeting(gd, player1Id, card, List.of(effect), effect);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetCardIds()).isEmpty();
        verify(gameLogService).append(eq(gd), argThat((GameLogEntry e) -> e.plainText().equals("Tilling Treefolk's enter-the-battlefield ability triggers.")));
    }

    @Test
    @DisplayName("handleReturnToHandETBTargeting prompts a multi-graveyard choice capped at maxTargets")
    void handleReturnToHandETBTargeting_promptsMultiGraveyardChoice() {
        Card card = new Card();
        card.setName("Tilling Treefolk");
        ReturnTargetCardsFromGraveyardToHandEffect effect =
                new ReturnTargetCardsFromGraveyardToHandEffect(new CardTypePredicate(CardType.LAND), 2);

        Card forest = new Card();
        forest.setName("Forest");
        forest.setType(CardType.LAND);
        gd.playerGraveyards.get(player1Id).add(forest);
        when(predicateEvaluationService.matchesCardPredicate(eq(forest), eq(effect.filter()), eq(card.getId())))
                .thenReturn(true);

        service.handleReturnToHandETBTargeting(gd, player1Id, card, List.of(effect), effect);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.graveyardTargetOperation.card).isSameAs(card);
        assertThat(gd.graveyardTargetOperation.controllerId).isEqualTo(player1Id);
        // Only one matching card, so the cap is min(2, 1) = 1
        verify(playerInputService).beginMultiGraveyardChoice(eq(gd), eq(player1Id), org.mockito.ArgumentMatchers.anyList(),
                eq(1), eq(0), anyString());
    }

    @Test
    @DisplayName("handleUpToNGraveyardSpellTargeting honors a required minimum")
    void handleUpToNGraveyardSpellTargeting_honorsRequiredMinimum() {
        Card card = new Card();
        card.setName("Peerless Recycling");
        ReturnTargetCardsFromGraveyardToHandEffect effect =
                new ReturnTargetCardsFromGraveyardToHandEffect(new CardTypePredicate(CardType.CREATURE), 2);
        Card first = new Card();
        first.setName("First Creature");
        first.setType(CardType.CREATURE);
        Card second = new Card();
        second.setName("Second Creature");
        second.setType(CardType.CREATURE);
        gd.playerGraveyards.get(player1Id).addAll(List.of(first, second));
        when(predicateEvaluationService.matchesCardPredicate(eq(first), eq(effect.filter()), eq(card.getId())))
                .thenReturn(true);
        when(predicateEvaluationService.matchesCardPredicate(eq(second), eq(effect.filter()), eq(card.getId())))
                .thenReturn(true);

        service.handleUpToNGraveyardSpellTargeting(gd, player1Id, card, StackEntryType.INSTANT_SPELL,
                effect, 2, List.of(effect), 2);

        verify(playerInputService).beginMultiGraveyardChoice(eq(gd), eq(player1Id), any(), eq(2), eq(2), anyString());
    }

    @Test
    @DisplayName("handleBeginningOfCombatGraveyardTargeting pushes stack entry when no matching graveyard cards")
    void handleBeginningOfCombatGraveyardTargeting_pushesStackEntryWhenGraveyardEmpty() {
        Card card = new Card();
        card.setName("Ravenous Chupacabra");
        UUID sourcePermanentId = UUID.randomUUID();
        ExileGraveyardCardsEffect exileEffect = new ExileGraveyardCardsEffect(
                1, GraveyardExileScope.TARGET_CARDS_ANY_GRAVEYARD, new CardTypePredicate(CardType.CREATURE));
        List<CardEffect> effects = List.of(exileEffect);

        service.handleBeginningOfCombatGraveyardTargeting(gd, player1Id, card, effects, sourcePermanentId, exileEffect);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(card);
    }

    @Test
    @DisplayName("handleBeginningOfCombatGraveyardTargeting skips non-matching card types in graveyard")
    void handleBeginningOfCombatGraveyardTargeting_skipsNonMatchingTypesInGraveyard() {
        Card card = new Card();
        card.setName("Ravenous Chupacabra");
        UUID sourcePermanentId = UUID.randomUUID();
        ExileGraveyardCardsEffect exileEffect = new ExileGraveyardCardsEffect(
                1, GraveyardExileScope.TARGET_CARDS_ANY_GRAVEYARD, new CardTypePredicate(CardType.CREATURE));

        Card landCard = new Card();
        landCard.setName("Forest");
        landCard.setType(CardType.LAND);
        gd.playerGraveyards.get(player1Id).add(landCard);

        service.handleBeginningOfCombatGraveyardTargeting(gd, player1Id, card, List.of(exileEffect), sourcePermanentId, exileEffect);

        // No creature in graveyard — falls through to empty-target stack entry path
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("graveyard target discovery unwraps optional mana payments")
    void graveyardTargetDiscovery_unwrapsMayPayManaEffect() {
        ReturnCardFromGraveyardEffect returnEffect = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .targetGraveyard(true)
                .build();
        MayPayManaEffect mayPay = new MayPayManaEffect("{W}{B}", 2, returnEffect,
                "Pay {W}{B} and 2 life?");

        GraveyardTargetingSupport.Target target = new GraveyardTargetingSupport().findTarget(List.of(mayPay));

        assertThat(target).isNotNull();
        assertThat(target.filter()).isSameAs(returnEffect.filter());
    }
}
