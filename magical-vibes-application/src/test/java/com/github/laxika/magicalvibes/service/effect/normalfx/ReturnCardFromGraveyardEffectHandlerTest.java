package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;

@ExtendWith(MockitoExtension.class)
class ReturnCardFromGraveyardEffectHandlerTest {

    @Mock
    private BattlefieldEntryService battlefieldEntryService;
    @Mock
    private PermanentRemovalService permanentRemovalService;
    @Mock
    private LegendRuleService legendRuleService;
    @Mock
    private GameQueryService gameQueryService;
    @Mock
    private PredicateEvaluationService predicateEvaluationService;
    @Mock
    private GameLogService gameLogService;
    @Mock
    private PlayerInputService playerInputService;
    @Mock
    private LifeSupport lifeSupport;
    @Mock
    private ExileService exileService;
    @Mock
    private GraveyardService graveyardService;
    @Mock
    private InteractionHandlerRegistry interactionHandlerRegistry;
    @InjectMocks
    private GraveyardReturnSupport support;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private ReturnCardFromGraveyardEffectHandler returnCardFromGraveyardHandler;

    @BeforeEach
    void setUp() {

        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.orderedPlayerIds.add(player1Id);
        gd.orderedPlayerIds.add(player2Id);
        gd.playerIds.add(player1Id);
        gd.playerIds.add(player2Id);
        gd.playerIdToName.put(player1Id, "Player1");
        gd.playerIdToName.put(player2Id, "Player2");
        gd.playerBattlefields.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerBattlefields.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerGraveyards.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerGraveyards.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerHands.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerHands.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerDecks.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerDecks.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        lenient().doAnswer(invocation -> {
            UUID handOwnerId = invocation.getArgument(2);
            Card card = invocation.getArgument(3);
            gd.addCardToHand(handOwnerId, card);
            return null;
        }).when(permanentRemovalService).addCardToHandFromGraveyard(
                eq(gd), any(UUID.class), any(UUID.class), any(Card.class));
        returnCardFromGraveyardHandler = new ReturnCardFromGraveyardEffectHandler(playerInputService, support);

    }

    private static Card createCard(String name) {
            Card card = new Card();
            card.setName(name);
            return card;
        }

        // =========================================================================
        // describeFilter A?€�t static utility method
        // =========================================================================

    @Test
            @DisplayName("null predicate returns 'card'")
            void nullPredicateReturnsCard() {
                assertThat(CardPredicateUtils.describeFilter(null)).isEqualTo("card");
            }

            @Test
            @DisplayName("CardTypePredicate(CREATURE) returns 'creature card'")
            void cardTypePredicateCreature() {
                CardPredicate predicate = new CardTypePredicate(CardType.CREATURE);
                assertThat(CardPredicateUtils.describeFilter(predicate)).isEqualTo("creature card");
            }

            @Test
            @DisplayName("CardTypePredicate(ARTIFACT) returns 'artifact card'")
            void cardTypePredicateArtifact() {
                CardPredicate predicate = new CardTypePredicate(CardType.ARTIFACT);
                assertThat(CardPredicateUtils.describeFilter(predicate)).isEqualTo("artifact card");
            }

            @Test
            @DisplayName("CardSubtypePredicate returns subtype display name + 'card'")
            void cardSubtypePredicate() {
                CardPredicate predicate = new CardSubtypePredicate(CardSubtype.ZOMBIE);
                assertThat(CardPredicateUtils.describeFilter(predicate)).isEqualTo("Zombie card");
            }

            @Test
            @DisplayName("CardKeywordPredicate returns 'card with <keyword>'")
            void cardKeywordPredicate() {
                CardPredicate predicate = new CardKeywordPredicate(Keyword.INFECT);
                assertThat(CardPredicateUtils.describeFilter(predicate)).isEqualTo("card with infect");
            }

            @Test
            @DisplayName("CardKeywordPredicate with multi-word keyword uses spaces")
            void cardKeywordPredicateMultiWord() {
                CardPredicate predicate = new CardKeywordPredicate(Keyword.FIRST_STRIKE);
                assertThat(CardPredicateUtils.describeFilter(predicate)).isEqualTo("card with first strike");
            }

            @Test
            @DisplayName("CardIsAuraPredicate returns 'Aura card'")
            void cardIsAuraPredicate() {
                CardPredicate predicate = new CardIsAuraPredicate();
                assertThat(CardPredicateUtils.describeFilter(predicate)).isEqualTo("Aura card");
            }

            @Test
            @DisplayName("CardAllOfPredicate combines descriptions merging 'card with' parts")
            void cardAllOfPredicateMerges() {
                CardPredicate predicate = new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardKeywordPredicate(Keyword.INFECT)
                ));
                assertThat(CardPredicateUtils.describeFilter(predicate))
                        .isEqualTo("creature card with infect");
            }

            @Test
            @DisplayName("CardAnyOfPredicate with same suffix merges to 'X or Y card'")
            void cardAnyOfPredicateMergesSuffix() {
                CardPredicate predicate = new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.ARTIFACT),
                        new CardTypePredicate(CardType.CREATURE)
                ));
                assertThat(CardPredicateUtils.describeFilter(predicate))
                        .isEqualTo("artifact or creature card");
            }

            @Test
            @DisplayName("CardAnyOfPredicate with mixed predicates joins with 'or'")
            void cardAnyOfPredicateMixed() {
                CardPredicate predicate = new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardIsAuraPredicate()
                ));
                assertThat(CardPredicateUtils.describeFilter(predicate))
                        .isEqualTo("creature or Aura card");
            }


    @Test
            @DisplayName("Returns pre-targeted creature card from graveyard to hand")
            void returnsPreTargetedCreatureToHand() {
                Card creature = createCard("Grizzly Bears");

                ReturnCardFromGraveyardEffect effect = ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .targetGraveyard(true)
                        .build();
                StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Recover"),
                        player1Id, "Recover", new ArrayList<>(List.of(effect)),
                        creature.getId(), Zone.GRAVEYARD);

                when(gameQueryService.findCardInGraveyardById(gd, creature.getId())).thenReturn(creature);

                returnCardFromGraveyardHandler.resolve(gd, entry, effect);

                verify(permanentRemovalService).removeCardFromGraveyardById(gd, creature.getId());
                assertThat(gd.playerHands.get(player1Id)).extracting(Card::getName)
                        .containsExactly("Grizzly Bears");
                verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) ->
                        logEntry.plainText().contains("Grizzly Bears") && logEntry.plainText().contains("graveyard to hand")));
            }

            @Test
            @DisplayName("Gains life equal to mana value when effect specifies it")
            void gainsLifeEqualToManaValue() {
                Card creature = createCard("Grizzly Bears");
                creature.setManaCost("{1}{G}");

                ReturnCardFromGraveyardEffect effect = ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .targetGraveyard(true)
                        .gainLifeEqualToManaValue(true)
                        .build();
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Razor Hippogriff"),
                        player1Id, "Razor Hippogriff", new ArrayList<>(List.of(effect)),
                        creature.getId(), Zone.GRAVEYARD);

                when(gameQueryService.findCardInGraveyardById(gd, creature.getId())).thenReturn(creature);

                returnCardFromGraveyardHandler.resolve(gd, entry, effect);

                verify(permanentRemovalService).removeCardFromGraveyardById(gd, creature.getId());
                verify(lifeSupport).applyGainLife(gd, player1Id, 2);
            }

            @Test
            @DisplayName("Fizzles when pre-targeted card is no longer in graveyard")
            void fizzlesWhenTargetGone() {
                UUID targetId = UUID.randomUUID();

                ReturnCardFromGraveyardEffect effect = ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .targetGraveyard(true)
                        .build();
                StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Recover"),
                        player1Id, "Recover", new ArrayList<>(List.of(effect)),
                        targetId, Zone.GRAVEYARD);

                when(gameQueryService.findCardInGraveyardById(gd, targetId)).thenReturn(null);

                returnCardFromGraveyardHandler.resolve(gd, entry, effect);

                assertThat(gd.playerHands.get(player1Id)).isEmpty();
                verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) ->
                        logEntry.plainText().contains("fizzles")));
            }


    @Test
            @DisplayName("Prompts graveyard choice when matching cards exist in any graveyard")
            void promptsChoiceWhenMatchingCardsExist() {
                Card creature = createCard("Grizzly Bears");
                Card artifact = createCard("Leonin Scimitar");
                gd.playerGraveyards.get(player1Id).add(creature);
                gd.playerGraveyards.get(player2Id).add(artifact);

                CardPredicate filter = new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.ARTIFACT),
                        new CardTypePredicate(CardType.CREATURE)
                ));
                ReturnCardFromGraveyardEffect effect = ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                        .filter(filter)
                        .build();
                StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Beacon of Unrest"),
                        player1Id, "Beacon of Unrest", new ArrayList<>(List.of(effect)));

                when(predicateEvaluationService.matchesCardPredicate(
                        eq(creature), eq(filter), eq(entry.getCard().getId()), eq(gd), isNull(),
                        isNull(), isNull(), anyInt())).thenReturn(true);
                when(predicateEvaluationService.matchesCardPredicate(
                        eq(artifact), eq(filter), eq(entry.getCard().getId()), eq(gd), isNull(),
                        isNull(), isNull(), anyInt())).thenReturn(true);

                returnCardFromGraveyardHandler.resolve(gd, entry, effect);

                verify(interactionHandlerRegistry).begin(eq(gd), argThat(i ->
                        i instanceof PendingInteraction.GraveyardChoice gc
                                && gc.playerId().equals(player1Id)));
            }

            @Test
            @DisplayName("Restricts an event-card graveyard choice to cards recorded by the event")
            void restrictsChoiceToEventCardIds() {
                Card unrelatedCreature = createCard("Unrelated Creature");
                Card destroyedCreature = createCard("Destroyed Creature");
                gd.playerGraveyards.get(player1Id).add(unrelatedCreature);
                gd.playerGraveyards.get(player2Id).add(destroyedCreature);

                CardPredicate filter = new CardTypePredicate(CardType.CREATURE);
                ReturnCardFromGraveyardEffect effect = ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                        .filter(filter)
                        .eventCardIdsOnly(true)
                        .build();
                StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Zero Point Ballad"),
                        player1Id, "Zero Point Ballad", new ArrayList<>(List.of(effect)));
                entry.setEventCardIds(List.of(destroyedCreature.getId()));

                when(predicateEvaluationService.matchesCardPredicate(
                        eq(destroyedCreature), eq(filter), eq(entry.getCard().getId()), eq(gd), isNull(),
                        isNull(), isNull(), anyInt())).thenReturn(true);

                returnCardFromGraveyardHandler.resolve(gd, entry, effect);

                verify(interactionHandlerRegistry).begin(eq(gd), argThat(i ->
                        i instanceof PendingInteraction.GraveyardChoice gc
                                && gc.cardPool().equals(List.of(destroyedCreature))));
                verify(predicateEvaluationService, never()).matchesCardPredicate(
                        eq(unrelatedCreature), eq(filter), eq(entry.getCard().getId()), eq(gd), isNull(),
                        isNull(), isNull(), anyInt());
            }

            @Test
            @DisplayName("Marks mandatory resolution-time graveyard choices as mandatory")
            void marksMandatoryChoiceAsMandatory() {
                Card creature = createCard("Grizzly Bears");
                gd.playerGraveyards.get(player1Id).add(creature);

                CardPredicate filter = new CardTypePredicate(CardType.CREATURE);
                ReturnCardFromGraveyardEffect effect = ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(filter)
                        .mandatory(true)
                        .build();
                StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Beacon of Unrest"),
                        player1Id, "Beacon of Unrest", new ArrayList<>(List.of(effect)));

                when(predicateEvaluationService.matchesCardPredicate(
                        eq(creature), eq(filter), eq(entry.getCard().getId()), eq(gd), isNull(),
                        isNull(), isNull(), anyInt())).thenReturn(true);

                returnCardFromGraveyardHandler.resolve(gd, entry, effect);

                verify(interactionHandlerRegistry).begin(eq(gd), argThat(i ->
                        i instanceof PendingInteraction.GraveyardChoice gc && gc.mandatory()));
            }

            @Test
            @DisplayName("Logs message and removes shuffle effect when no matching cards in any graveyard")
            void logsMessageWhenNoMatchingCards() {
                CardPredicate filter = new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.ARTIFACT),
                        new CardTypePredicate(CardType.CREATURE)
                ));
                ReturnCardFromGraveyardEffect effect = ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                        .filter(filter)
                        .build();
                ShuffleIntoLibraryEffect shuffleEffect = new ShuffleIntoLibraryEffect();
                StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Beacon of Unrest"),
                        player1Id, "Beacon of Unrest", new ArrayList<>(List.of(effect, shuffleEffect)));

                returnCardFromGraveyardHandler.resolve(gd, entry, effect);

                verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) ->
                        logEntry.plainText().contains("no ") && logEntry.plainText().contains("in any graveyard")));
                // Shuffle effect should be removed when no valid targets
                assertThat(entry.getEffectsToResolve()).noneMatch(e -> e instanceof ShuffleIntoLibraryEffect);
            }

            @Test
            @DisplayName("Does not prompt when all graveyard cards fail predicate")
            void doesNotPromptWhenNoCardsMatchPredicate() {
                Card creature = createCard("Grizzly Bears");
                gd.playerGraveyards.get(player1Id).add(creature);

                CardPredicate filter = new CardTypePredicate(CardType.ARTIFACT);
                ReturnCardFromGraveyardEffect effect = ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                        .filter(filter)
                        .build();
                StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Beacon of Unrest"),
                        player1Id, "Beacon of Unrest", new ArrayList<>(List.of(effect)));

                when(predicateEvaluationService.matchesCardPredicate(
                        eq(creature), eq(filter), eq(entry.getCard().getId()), eq(gd), isNull(),
                        isNull(), isNull(), anyInt())).thenReturn(false);

                returnCardFromGraveyardHandler.resolve(gd, entry, effect);

                verify(interactionHandlerRegistry, never()).begin(any(), any());
                verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) ->
                        logEntry.plainText().contains("no ") && logEntry.plainText().contains("in any graveyard")));
            }

            @Test
            @DisplayName("Filters resolution-time choices by mana value at most X")
            void filtersResolutionTimeChoicesByManaValueAtMostX() {
                Card expensive = createCard("Colossal Dreadmaw");
                expensive.setManaCost("{6}{G}");
                Card eligible = createCard("Grizzly Bears");
                eligible.setManaCost("{1}{G}");
                gd.playerGraveyards.get(player1Id).add(expensive);
                gd.playerGraveyards.get(player1Id).add(eligible);

                CardPredicate filter = new CardTypePredicate(CardType.CREATURE);
                ReturnCardFromGraveyardEffect effect = ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(filter)
                        .requiresManaValueAtMostX(true)
                        .enterTapped(true)
                        .build();
                StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, createCard("Black Sun's Twilight"),
                        player1Id, "Black Sun's Twilight", new ArrayList<>(List.of(effect)), 5);

                when(predicateEvaluationService.matchesCardPredicate(
                        any(Card.class), eq(filter), eq(entry.getCard().getId()), eq(gd), isNull(),
                        isNull(), isNull(), anyInt()))
                        .thenReturn(true);

                returnCardFromGraveyardHandler.resolve(gd, entry, effect);

                verify(interactionHandlerRegistry).begin(eq(gd), argThat(i ->
                        i instanceof PendingInteraction.GraveyardChoice gc
                                && gc.validIndices().equals(List.of(1))));
            }

    @Test
    @DisplayName("Preserves mandatory and tapped flags on a resolution-time graveyard choice")
    void preservesMandatoryAndTappedFlagsOnResolutionTimeChoice() {
        Card land = createCard("Forest");
        gd.playerGraveyards.get(player1Id).add(land);
        CardPredicate filter = new CardTypePredicate(CardType.LAND);
        ReturnCardFromGraveyardEffect effect = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(filter)
                .mandatory(true)
                .enterTapped(true)
                .build();
        StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Test source"),
                player1Id, "Test source", new ArrayList<>(List.of(effect)));

        when(predicateEvaluationService.matchesCardPredicate(
                eq(land), eq(filter), eq(entry.getCard().getId()), eq(gd),
                isNull(), isNull(), isNull(), eq(0))).thenReturn(true);

        returnCardFromGraveyardHandler.resolve(gd, entry, effect);

        verify(interactionHandlerRegistry).begin(eq(gd), argThat(interaction ->
                interaction instanceof PendingInteraction.GraveyardChoice choice
                        && choice.mandatory()
                        && choice.enterTapped()
                        && choice.validIndices().equals(List.of(0))));
    }
}
