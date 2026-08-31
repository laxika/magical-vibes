package com.github.laxika.magicalvibes.service.input;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingDubiousChallengeChoice;
import com.github.laxika.magicalvibes.model.PendingMurmursFromBeyondChoice;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.WarpWorldService;
import com.github.laxika.magicalvibes.service.state.StateBasedActionService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.LibraryRevealChoiceInteractionHandler;
import com.github.laxika.magicalvibes.service.interaction.LibraryReorderInteractionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibraryChoiceHandlerServiceTest {

    @Mock private SessionManager sessionManager;
    @Mock private GameQueryService gameQueryService;
    @Mock private GraveyardService graveyardService;
    @Mock private BattlefieldEntryService battlefieldEntryService;
    @Mock private LegendRuleService legendRuleService;
    @Mock private StateBasedActionService stateBasedActionService;
    @Mock private GameLogService gameLogService;
    @Mock private CardViewFactory cardViewFactory;
    @Mock private InputCompletionService inputCompletionService;
    @Mock private PlayerInputService playerInputService;
    @Mock private EffectResolutionService effectResolutionService;
    @Mock private ExileService exileService;
    @Mock private PredicateEvaluationService predicateEvaluationService;
    @Mock private com.github.laxika.magicalvibes.service.effect.normalfx.PermanentControlSupport permanentControlSupport;
    @Mock private com.github.laxika.magicalvibes.service.effect.normalfx.MurmursFromBeyondEffectHandler murmursFromBeyondEffectHandler;
    @Mock private com.github.laxika.magicalvibes.service.effect.normalfx.AnimalMagnetismEffectHandler animalMagnetismEffectHandler;
    @Mock private com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport permanentCounterSupport;

    private LibraryChoiceHandlerService service;

    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry registry =
                new com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry(
                        () -> mock(com.github.laxika.magicalvibes.service.event.GameMutationCoordinator.class));
        registry.register(new com.github.laxika.magicalvibes.service.interaction.LibrarySearchInteractionHandler(
                mock(LibraryChoiceHandlerService.class)));
        service = new LibraryChoiceHandlerService(gameQueryService,
                predicateEvaluationService,
                graveyardService, battlefieldEntryService, legendRuleService,
                stateBasedActionService, gameLogService, inputCompletionService,
                playerInputService, effectResolutionService, exileService, registry,
                mock(com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService.class),
                mock(com.github.laxika.magicalvibes.service.effect.normalfx.LibrarySearchSupport.class),
                mock(com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport.class),
                mock(com.github.laxika.magicalvibes.service.DrawService.class),
                mock(com.github.laxika.magicalvibes.service.effect.normalfx.AnimationSupport.class),
                murmursFromBeyondEffectHandler,
                animalMagnetismEffectHandler,
                mock(com.github.laxika.magicalvibes.service.effect.AmountEvaluationService.class),
                mock(com.github.laxika.magicalvibes.service.effect.normalfx.BasicLandSearchQueueSupport.class),
                mock(com.github.laxika.magicalvibes.service.effect.normalfx.GuildFeudSupport.class),
                mock(com.github.laxika.magicalvibes.service.effect.normalfx.ReturnCardExiledWithSourceToBattlefieldEffectHandler.class),
                permanentControlSupport, permanentCounterSupport);
        registry.register(new LibraryRevealChoiceInteractionHandler(service));
        registry.register(new LibraryReorderInteractionHandler(
                gameLogService, mock(WarpWorldService.class), inputCompletionService));
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        player1 = new Player(player1Id, "Player1");
        player2 = new Player(player2Id, "Player2");
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
        gd.activePlayerId = player1Id;
    }

    @Nested
    @DisplayName("handleLibraryRevealChoice for Dubious Challenge")
    class HandleDubiousChallengeChoice {

        @Test
        @DisplayName("Exiles the controller's selection and prompts the targeted opponent")
        void initialChoicePromptsOpponent() {
            Card creature1 = createCard("Creature One", CardType.CREATURE);
            Card creature2 = createCard("Creature Two", CardType.CREATURE);
            Card land = createCard("Forest", CardType.LAND);
            List<Card> lookedAt = List.of(creature1, creature2, land);
            gd.playerDecks.get(player1Id).addAll(lookedAt);

            gd.queueInteraction(new PendingDubiousChallengeChoice(player1Id, player2Id, List.of()));
            gd.interaction.beginInteraction(new PendingInteraction.LibraryRevealChoice(
                    player1Id, lookedAt, List.of(creature1.getId(), creature2.getId()),
                    false, false, false, false, false, 0, null, 2,
                    "Choose up to two creature cards.", 0, false));

            service.handleLibraryRevealChoice(gd, player1, List.of(creature1.getId(), creature2.getId()));

            assertThat(gd.playerDecks.get(player1Id)).containsExactly(land);
            verify(exileService).exileCard(gd, player1Id, creature1);
            verify(exileService).exileCard(gd, player1Id, creature2);
            PendingInteraction.LibraryRevealChoice opponentChoice =
                    gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
            assertThat(opponentChoice.playerId()).isEqualTo(player2Id);
            assertThat(opponentChoice.validCardIds()).containsExactly(creature1.getId(), creature2.getId());
        }

        @Test
        @DisplayName("Puts the opponent's choice under the opponent's control and the rest under the controller's")
        void opponentChoicePlacesCardsUnderCorrectPlayers() {
            Card opponentCard = createCard("Opponent Creature", CardType.CREATURE);
            Card controllerCard = createCard("Controller Creature", CardType.CREATURE);
            gd.addToExile(player1Id, opponentCard);
            gd.addToExile(player1Id, controllerCard);
            gd.queueInteraction(new PendingDubiousChallengeChoice(
                    player1Id, player2Id, List.of(opponentCard, controllerCard)));
            gd.interaction.beginInteraction(new PendingInteraction.LibraryRevealChoice(
                    player2Id, List.of(opponentCard, controllerCard),
                    List.of(opponentCard.getId(), controllerCard.getId()),
                    false, false, false, false, false, 0, null, 1,
                    "Choose one.", 0, false));

            service.handleLibraryRevealChoice(gd, player2, List.of(opponentCard.getId()));

            verify(battlefieldEntryService).putPermanentOntoBattlefield(
                    eq(gd), eq(player2Id), any(), any(), any());
            verify(battlefieldEntryService).putPermanentOntoBattlefield(
                    eq(gd), eq(player1Id), any(), any(), any());
            assertThat(gd.exiledCards).isEmpty();
            verify(stateBasedActionService).performStateBasedActions(gd);
            verify(inputCompletionService).processMayAbilitiesThenAutoPassPreservingPriority(gd);
        }
    }

    @Test
    @DisplayName("Routes Murmurs from Beyond's opponent choice through its effect handler")
    void murmursFromBeyondChoiceUsesEffectHandler() {
        Card first = createCard("First Card");
        Card second = createCard("Second Card");
        Card third = createCard("Third Card");
        List<Card> revealed = List.of(first, second, third);
        gd.queueInteraction(new PendingMurmursFromBeyondChoice(player1Id));
        gd.interaction.beginInteraction(new PendingInteraction.LibraryRevealChoice(
                player2Id, revealed,
                List.of(first.getId(), second.getId(), third.getId()),
                false, false, false, false, false, 0, null, 1,
                "Choose one.", 1, false));

        service.handleLibraryRevealChoice(gd, player2, List.of(second.getId()));

        verify(murmursFromBeyondEffectHandler).completeCardChoice(gd, revealed, List.of(second.getId()));
        verify(inputCompletionService).processMayAbilitiesThenAutoPassPreservingPriority(gd);
    }

    @Test
    @DisplayName("Routes Animal Magnetism's opponent choice through its effect handler")
    void animalMagnetismChoiceUsesEffectHandler() {
        Card first = createCard("First Card", CardType.CREATURE);
        Card second = createCard("Second Card", CardType.CREATURE);
        Card third = createCard("Third Card");
        List<Card> revealed = List.of(first, second, third);
        gd.queueInteraction(new com.github.laxika.magicalvibes.model.PendingAnimalMagnetismChoice(player1Id));
        gd.interaction.beginInteraction(new PendingInteraction.LibraryRevealChoice(
                player2Id, revealed,
                List.of(first.getId(), second.getId()),
                true, false, false, false, false, 0, null, 1,
                "Choose one.", false, 1, false));

        service.handleLibraryRevealChoice(gd, player2, List.of(second.getId()));

        verify(animalMagnetismEffectHandler).completeCardChoice(gd, revealed, List.of(second.getId()));
        verify(inputCompletionService).processMayAbilitiesThenAutoPassPreservingPriority(gd);
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private static Card createCard(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }

    private static Card createCard(String name, CardType type) {
        Card card = createCard(name);
        card.setType(type);
        return card;
    }

    @Test
    @DisplayName("Puts the requested counter on a card entering from a library")
    void putsCounterOnLibraryCardEnteringBattlefield() {
        Card forest = createBasicLand("Forest");
        gd.playerDecks.get(player1Id).add(forest);
        LibrarySearchParams params = LibrarySearchParams.builder(player1Id, List.of(forest))
                .canFailToFind(true)
                .destination(LibrarySearchDestination.BATTLEFIELD_TAPPED)
                .battlefieldCounter(CounterType.STUN)
                .build();
        gd.interaction.beginInteraction(new PendingInteraction.LibrarySearch(params, "Choose a land", true));

        service.handleLibraryCardChosen(gd, player1, 0);

        verify(permanentCounterSupport).placeCounterOnPermanent(
                eq(gd), isNull(), any(), eq(CounterType.STUN), eq(1));
    }

    @Test
    @DisplayName("Exile-and-create-tokens search keeps selecting after the source is gone")
    void exileAndCreateTokensSearchResumesAfterSourceSacrifice() {
        Card artifact1 = createCard("Artifact One", CardType.ARTIFACT);
        Card artifact2 = createCard("Artifact Two", CardType.ARTIFACT);
        gd.playerDecks.get(player1Id).addAll(List.of(artifact1, artifact2));
        CreateTokenEffect token = new CreateTokenEffect(
                1, "Myr", 1, 1, null, List.of(), Set.of(), Set.of(CardType.ARTIFACT));
        when(predicateEvaluationService.matchesCardPredicate(any(Card.class),
                any(com.github.laxika.magicalvibes.model.filter.CardPredicate.class), isNull(), eq(gd), eq(player1Id)))
                .thenReturn(true);
        LibrarySearchParams params = LibrarySearchParams.builder(player1Id, List.of(artifact1, artifact2))
                .canFailToFind(true)
                .destination(LibrarySearchDestination.EXILE_AND_CREATE_TOKENS)
                .filterPredicate(new CardTypePredicate(CardType.ARTIFACT))
                .tokenTemplate(token)
                .sourceSetCode("MRD")
                .build();
        gd.interaction.beginInteraction(new PendingInteraction.LibrarySearch(params, "Choose cards", true));

        service.handleLibraryCardChosen(gd, player1, 0);
        gd.interaction.beginInteraction(new PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(player1Id, List.of(artifact2))
                        .canFailToFind(true)
                        .destination(LibrarySearchDestination.EXILE_AND_CREATE_TOKENS)
                        .filterPredicate(new CardTypePredicate(CardType.ARTIFACT))
                        .accumulatedCards(List.of(artifact1))
                        .tokenTemplate(token)
                        .sourceSetCode("MRD")
                        .build(),
                "Choose cards", true));
        service.handleLibraryCardChosen(gd, player1, -1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1Id)).containsExactly(artifact2);
        verify(permanentControlSupport).applyCreateToken(gd, player1Id, token, 1, "MRD");
        verify(inputCompletionService).processMayAbilitiesThenAutoPassPreservingPriority(gd);
    }

    @Test
    @DisplayName("Dynamic card-type follow-up offers one card for each remaining type")
    void dynamicCardTypeFollowUpOffersEachRemainingType() {
        Card instant = createCard("Instant One", CardType.INSTANT);
        Card artifact = createCard("Artifact One", CardType.ARTIFACT);
        Card sorcery = createCard("Sorcery One", CardType.SORCERY);
        gd.playerDecks.get(player1Id).addAll(List.of(instant, artifact, sorcery));

        LibrarySearchParams params = LibrarySearchParams.builder(player1Id, List.of(instant))
                .reveals(true)
                .canFailToFind(true)
                .destination(LibrarySearchDestination.HAND)
                .sourceCards(new ArrayList<>(List.of(instant, artifact, sorcery)))
                .reorderRemainingToBottom(true)
                .followUp(LibrarySearchFollowUp.forCardTypeBoundedPick(
                        List.of(CardType.ARTIFACT, CardType.SORCERY)))
                .build();
        gd.interaction.beginInteraction(new PendingInteraction.LibrarySearch(
                params, "Choose an instant card", true));

        service.handleLibraryCardChosen(gd, player1, 0);

        PendingInteraction.LibrarySearch next =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(next.params().cards()).containsExactly(artifact);
        assertThat(next.params().cards()).doesNotContain(sorcery);
    }

    @Test
    @DisplayName("Puts one qualifying card into hand and the other revealed cards on the library bottom")
    void putsOneCardIntoHandAndBottomsTheRest() {
        Card chosen = createCard("Chosen");
        Card rest = createCard("Rest");
        Card land = createCard("Land", CardType.LAND);
        List<Card> sourceCards = new ArrayList<>(List.of(chosen, rest, land));
        gd.addToExile(player1Id, chosen);
        gd.addToExile(player1Id, rest);
        gd.addToExile(player1Id, land);
        gd.interaction.beginInteraction(new PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(player1Id, List.of(chosen, rest))
                        .reveals(true)
                        .sourceCards(sourceCards)
                        .reorderRemainingToBottom(true)
                        .shuffleAfterSelection(false)
                        .destination(LibrarySearchDestination.PUT_ONE_INTO_HAND_REST_TO_BOTTOM_RANDOM)
                        .build(),
                "Choose one", false));

        service.handleLibraryCardChosen(gd, player1, 0);

        assertThat(gd.playerHands.get(player1Id)).containsExactly(chosen);
        assertThat(gd.playerDecks.get(player1Id)).containsExactly(land);
        assertThat(gd.exiledCards).extracting(entry -> entry.card()).containsExactly(rest);
        verify(inputCompletionService).processMayAbilitiesThenAutoPassPreservingPriority(gd);
    }

    @Test
    @DisplayName("Face-down exile search puts the unchosen cards into the target player's graveyard")
    void faceDownExileSearchPutsRestIntoTargetGraveyard() {
        Card first = createCard("First");
        Card chosen = createCard("Chosen");
        Card third = createCard("Third");
        List<Card> sourceCards = new ArrayList<>(List.of(first, chosen, third));
        UUID sourcePermanentId = UUID.randomUUID();
        LibrarySearchParams params = LibrarySearchParams.builder(player1Id, List.of(first, chosen, third))
                .canFailToFind(false)
                .targetPlayerId(player2Id)
                .sourceCards(sourceCards)
                .restToGraveyard(true)
                .destination(LibrarySearchDestination.EXILE_ONE_FACE_DOWN_REST_TO_GRAVEYARD)
                .sourcePermanentId(sourcePermanentId)
                .build();
        gd.interaction.beginInteraction(new PendingInteraction.LibrarySearch(params, "Choose one", false));

        service.handleLibraryCardChosen(gd, player1, 1);

        verify(exileService).exileCardFaceDown(gd, player2Id, chosen, sourcePermanentId);
        verify(graveyardService).addCardToGraveyard(gd, player2Id, first, Zone.LIBRARY);
        verify(graveyardService).addCardToGraveyard(gd, player2Id, third, Zone.LIBRARY);
        assertThat(gd.exilePlayPermissions).containsEntry(chosen.getId(), player1Id);
        verify(inputCompletionService).processMayAbilitiesThenAutoPassPreservingPriority(gd);
    }

    @Test
    @DisplayName("Bounded face-down exile search takes two cards before bottoming the rest")
    void boundedFaceDownExileSearchTakesTwoCards() {
        Card first = createCard("First");
        Card second = createCard("Second");
        Card third = createCard("Third");
        List<Card> sourceCards = new ArrayList<>(List.of(first, second, third));
        UUID sourcePermanentId = UUID.randomUUID();
        LibrarySearchParams params = LibrarySearchParams.builder(player1Id, new ArrayList<>(sourceCards))
                .canFailToFind(false)
                .targetPlayerId(player2Id)
                .remainingCount(2)
                .sourceCards(sourceCards)
                .reorderRemainingToBottom(true)
                .shuffleAfterSelection(false)
                .destination(LibrarySearchDestination.EXILE_TWO_FACE_DOWN_REST_TO_BOTTOM_RANDOM)
                .sourcePermanentId(sourcePermanentId)
                .build();
        gd.interaction.beginInteraction(new PendingInteraction.LibrarySearch(params, "Choose two", false));

        service.handleLibraryCardChosen(gd, player1, 0);

        PendingInteraction.LibrarySearch next =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(next.params().cards()).containsExactly(second, third);
        assertThat(next.params().remainingCount()).isEqualTo(1);

        service.handleLibraryCardChosen(gd, player1, 0);

        verify(exileService).exileCardFaceDown(gd, player2Id, first, sourcePermanentId);
        verify(exileService).exileCardFaceDown(gd, player2Id, second, sourcePermanentId);
        assertThat(gd.playerDecks.get(player2Id)).containsExactly(third);
    }

    @Test
    @DisplayName("Face-down exile search can leave casting permission to a source static effect")
    void faceDownExileSearchCanSkipSeparateCastPermission() {
        Card first = createCard("First");
        Card chosen = createCard("Chosen");
        Card third = createCard("Third");
        UUID sourcePermanentId = UUID.randomUUID();
        LibrarySearchParams params = LibrarySearchParams.builder(player1Id, List.of(first, chosen, third))
                .canFailToFind(false)
                .sourceCards(new ArrayList<>(List.of(first, chosen, third)))
                .reorderRemainingToBottom(true)
                .destination(LibrarySearchDestination.EXILE_ONE_FACE_DOWN_REST_TO_BOTTOM_RANDOM)
                .sourcePermanentId(sourcePermanentId)
                .grantExilePlayPermission(false)
                .allowAnyManaType(false)
                .build();
        gd.interaction.beginInteraction(new PendingInteraction.LibrarySearch(params, "Choose one", false));

        service.handleLibraryCardChosen(gd, player1, 1);

        verify(exileService).exileCardFaceDown(gd, player1Id, chosen, sourcePermanentId);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(chosen.getId());
        assertThat(gd.exilePlayAnyManaTypeWhileExiled).doesNotContain(chosen.getId());
        verify(inputCompletionService).processMayAbilitiesThenAutoPassPreservingPriority(gd);
    }

    private static Card createBasicLand(String name) {
        Card card = createCard(name, CardType.LAND);
        card.setSupertypes(Set.of(CardSupertype.BASIC));
        return card;
    }

    private void stubCardViewFactory() {
        lenient().when(cardViewFactory.create(any(Card.class))).thenReturn(mock(CardView.class));
    }

    /**
     * Sets up a library search interaction for a player searching for a basic land
     * to put onto the battlefield (the state left by Field of Ruin's resolution).
     */
    private void beginBasicLandBattlefieldSearch(UUID playerId, List<Card> searchCards) {
        beginBasicLandBattlefieldSearch(playerId, searchCards, LibrarySearchFollowUp.NONE);
    }

    private void beginBasicLandBattlefieldSearch(UUID playerId, List<Card> searchCards, LibrarySearchFollowUp followUp) {
        LibrarySearchParams params = LibrarySearchParams.builder(playerId, searchCards)
                .reveals(false)
                .canFailToFind(true)
                .destination(LibrarySearchDestination.BATTLEFIELD)
                .followUp(followUp)
                .build();
        gd.interaction.beginInteraction(new PendingInteraction.LibrarySearch(params, "Search your library for a basic land card and put it onto the battlefield.", true));
    }

    // =========================================================================
    // handleLibraryCardChosen — each-player basic-land-search follow-up processing
    // =========================================================================

    @Nested
    @DisplayName("handleLibraryCardChosen with each-player basic-land-search follow-up")
    class HandleLibraryCardChosenWithEachPlayerQueue {

        @Test
        @DisplayName("After successful choice, starts next player's search from follow-up")
        void successfulChoiceStartsNextPlayerSearch() {
            stubCardViewFactory();

            // Player1 is currently searching; player2 rides the follow-up remainder
            Card plains = createBasicLand("Plains");
            gd.playerDecks.get(player1Id).add(plains);
            beginBasicLandBattlefieldSearch(player1Id, List.of(plains),
                    LibrarySearchFollowUp.eachPlayerBasicLand(List.of(player2Id), false));

            Card forest = createBasicLand("Forest");
            gd.playerDecks.get(player2Id).add(forest);


            // Player1 picks index 0
            service.handleLibraryCardChosen(gd, player1, 0);

            // Player2 should now be prompted to search, with an exhausted remainder
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
            assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId()).isEqualTo(player2Id);
            assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                    .params().followUp().remainingEachPlayerBasicLandSearches()).isEmpty();
        }

        @Test
        @DisplayName("After fail-to-find, starts next player's search from follow-up")
        void failToFindStartsNextPlayerSearch() {
            stubCardViewFactory();

            // Player1 is currently searching; player2 rides the follow-up remainder
            Card plains = createBasicLand("Plains");
            beginBasicLandBattlefieldSearch(player1Id, List.of(plains),
                    LibrarySearchFollowUp.eachPlayerBasicLand(List.of(player2Id), false));
            gd.playerDecks.get(player1Id).add(plains);

            Card forest = createBasicLand("Forest");
            gd.playerDecks.get(player2Id).add(forest);

            // Player1 declines (-1)
            service.handleLibraryCardChosen(gd, player1, -1);

            // Player2 should now be prompted to search
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
            assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId()).isEqualTo(player2Id);
        }

        @Test
        @DisplayName("After last player in queue completes, resolves auto-pass")
        void lastPlayerCompletesResolvesAutoPass() {
            // Player2 is searching; follow-up remainder is empty
            Card forest = createBasicLand("Forest");
            gd.playerDecks.get(player2Id).add(forest);
            beginBasicLandBattlefieldSearch(player2Id, List.of(forest));

            service.handleLibraryCardChosen(gd, player2, 0);

            // Should resolve auto-pass since no searcher remains
            verify(inputCompletionService).processMayAbilitiesThenAutoPassPreservingPriority(gd);
        }

        @Test
        @DisplayName("Skips queued player with no basic lands and tries next")
        void skipsQueuedPlayerWithNoBasicLands() {
            stubCardViewFactory();

            // Player1 is currently searching
            Card plains = createBasicLand("Plains");
            gd.playerDecks.get(player1Id).add(plains);
            beginBasicLandBattlefieldSearch(player1Id, List.of(plains),
                    LibrarySearchFollowUp.eachPlayerBasicLand(List.of(player2Id), false));

            // Remainder: player2 has no basic lands
            gd.playerDecks.get(player2Id).add(createCard("Grizzly Bears", CardType.CREATURE));


            service.handleLibraryCardChosen(gd, player1, 0);

            // Player2 was skipped (no basic lands), auto-pass called
            InOrder order = inOrder(gameLogService, inputCompletionService);
            order.verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) ->
                    logEntry.plainText().contains("Player2") && logEntry.plainText().contains("finds no basic land cards")));
            order.verify(inputCompletionService).processMayAbilitiesThenAutoPassPreservingPriority(gd);
        }

        @Test
        @DisplayName("Skips queued player with empty library and tries next")
        void skipsQueuedPlayerWithEmptyLibrary() {
            stubCardViewFactory();

            // Player1 is currently searching
            Card plains = createBasicLand("Plains");
            gd.playerDecks.get(player1Id).add(plains);
            beginBasicLandBattlefieldSearch(player1Id, List.of(plains),
                    LibrarySearchFollowUp.eachPlayerBasicLand(List.of(player2Id), false));

            // Remainder: player2 has empty library (already empty from setUp)


            service.handleLibraryCardChosen(gd, player1, 0);

            // Player2 was skipped (empty library), auto-pass called
            verify(inputCompletionService).processMayAbilitiesThenAutoPassPreservingPriority(gd);
            verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) ->
                    logEntry.plainText().contains("Player2") && logEntry.plainText().contains("it is empty")));
        }

        @Test
        @DisplayName("Queue is not processed when it is empty")
        void emptyQueueDoesNothing() {
            // Player1 searches with no follow-up remainder
            Card plains = createBasicLand("Plains");
            gd.playerDecks.get(player1Id).add(plains);
            beginBasicLandBattlefieldSearch(player1Id, List.of(plains));


            service.handleLibraryCardChosen(gd, player1, 0);

            // No further library search, just auto-pass
            verify(inputCompletionService).processMayAbilitiesThenAutoPassPreservingPriority(gd);
        }

        @Test
        @DisplayName("Search only presents basic land cards to queued player")
        void searchOnlyPresentsBasicLandsToQueuedPlayer() {
            stubCardViewFactory();

            // Player1 is currently searching
            Card plains = createBasicLand("Plains");
            gd.playerDecks.get(player1Id).add(plains);
            beginBasicLandBattlefieldSearch(player1Id, List.of(plains),
                    LibrarySearchFollowUp.eachPlayerBasicLand(List.of(player2Id), false));

            // Remainder: player2 has mixed library
            gd.playerDecks.get(player2Id).addAll(List.of(
                    createBasicLand("Forest"),
                    createBasicLand("Island"),
                    createCard("Grizzly Bears", CardType.CREATURE),
                    createCard("Ghost Quarter", CardType.LAND) // nonbasic
            ));


            service.handleLibraryCardChosen(gd, player1, 0);

            // Player2 should only see basic land cards
            assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(2);
            assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                    .allMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC));
        }
    }

    // =========================================================================
    // handleLibraryCardChosen — resumes trailing spell effects (Exploding Borders)
    // =========================================================================

    @Nested
    @DisplayName("handleLibraryCardChosen resumes remaining effects on the paused spell")
    class ResumesTrailingSpellEffects {

        /** A paused spell whose search (index 0) has a trailing effect left at index 1. */
        private StackEntry pausedEntryWithTrailingEffect() {
            List<CardEffect> effects = List.of(new DrawCardEffect(1), new DrawCardEffect(1));
            return new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Exploding Borders"),
                    player1Id, "Exploding Borders", effects);
        }

        @Test
        @DisplayName("Successful pick resumes the paused stack entry's remaining effects, then auto-passes")
        void successfulPickResumesRemainingEffects() {
            stubCardViewFactory();
            Card plains = createBasicLand("Plains");
            gd.playerDecks.get(player1Id).add(plains);
            beginBasicLandBattlefieldSearch(player1Id, List.of(plains));

            StackEntry paused = pausedEntryWithTrailingEffect();
            gd.pendingEffectResolutionEntry = paused;
            gd.pendingEffectResolutionIndex = 1;

            service.handleLibraryCardChosen(gd, player1, 0);

            verify(effectResolutionService).resolveEffectsFrom(gd, paused, 1);
            verify(inputCompletionService).processMayAbilitiesThenAutoPassPreservingPriority(gd);
        }

        @Test
        @DisplayName("Fail-to-find still resumes the paused stack entry's remaining effects")
        void failToFindResumesRemainingEffects() {
            stubCardViewFactory();
            Card plains = createBasicLand("Plains");
            gd.playerDecks.get(player1Id).add(plains);
            beginBasicLandBattlefieldSearch(player1Id, List.of(plains));

            StackEntry paused = pausedEntryWithTrailingEffect();
            gd.pendingEffectResolutionEntry = paused;
            gd.pendingEffectResolutionIndex = 1;

            service.handleLibraryCardChosen(gd, player1, -1);

            verify(effectResolutionService).resolveEffectsFrom(gd, paused, 1);
            verify(inputCompletionService).processMayAbilitiesThenAutoPassPreservingPriority(gd);
        }

        @Test
        @DisplayName("Search-only spell (no pending entry) does not attempt a resume")
        void noPendingEntryDoesNotResume() {
            stubCardViewFactory();
            Card plains = createBasicLand("Plains");
            gd.playerDecks.get(player1Id).add(plains);
            beginBasicLandBattlefieldSearch(player1Id, List.of(plains));

            service.handleLibraryCardChosen(gd, player1, 0);

            verify(effectResolutionService, never()).resolveEffectsFrom(any(), any(), anyInt());
            verify(inputCompletionService).processMayAbilitiesThenAutoPassPreservingPriority(gd);
        }
    }

    // =========================================================================
    // handleLibraryRevealChoice — randomRemainingToBottom
    // =========================================================================

    @Nested
    @DisplayName("handleLibraryRevealChoice with randomRemainingToBottom")
    class HandleLibraryRevealChoiceRandomBottom {

        @Test
        @DisplayName("Selected cards go to battlefield, rest go to bottom of library (not graveyard)")
        void selectedToBattlefieldRestToBottom() {
            Card dino = createCard("Colossal Dreadmaw", CardType.CREATURE);
            Card land = createCard("Forest", CardType.LAND);
            Card instant = createCard("Shock", CardType.INSTANT);

            List<Card> allCards = List.of(dino, land, instant);
            Set<UUID> validIds = Set.of(dino.getId());

            gd.interaction.beginInteraction(new com.github.laxika.magicalvibes.model.PendingInteraction.LibraryRevealChoice(
                    player1Id, new ArrayList<>(allCards), new ArrayList<>(validIds),
                    false, false, false, true, false, 0, null, validIds.size(), "Choose."));
            when(battlefieldEntryService.snapshotEnterTappedTypes(gd)).thenReturn(Set.of());

            service.handleLibraryRevealChoice(gd, player1, List.of(dino.getId()));

            // Dino should have been put onto battlefield
            verify(battlefieldEntryService).putPermanentOntoBattlefield(eq(gd), eq(player1Id), any(), any());

            // Remaining cards should be on bottom of library (not in graveyard)
            assertThat(gd.playerDecks.get(player1Id)).hasSize(2);
            verify(graveyardService, never()).addCardToGraveyard(any(), any(), any());
        }

        @Test
        @DisplayName("Choosing zero puts all cards on bottom of library")
        void choosingZeroPutsAllOnBottom() {
            Card dino = createCard("Colossal Dreadmaw", CardType.CREATURE);
            Card land = createCard("Forest", CardType.LAND);

            List<Card> allCards = List.of(dino, land);
            Set<UUID> validIds = Set.of(dino.getId());

            gd.interaction.beginInteraction(new com.github.laxika.magicalvibes.model.PendingInteraction.LibraryRevealChoice(
                    player1Id, new ArrayList<>(allCards), new ArrayList<>(validIds),
                    false, false, false, true, false, 0, null, validIds.size(), "Choose."));

            service.handleLibraryRevealChoice(gd, player1, List.of());

            // Nothing put onto battlefield
            verify(battlefieldEntryService, never()).putPermanentOntoBattlefield(any(), any(), any());

            // All cards on bottom of library
            assertThat(gd.playerDecks.get(player1Id)).hasSize(2);
            verify(graveyardService, never()).addCardToGraveyard(any(), any(), any());
            verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) ->
                    logEntry.plainText().contains("bottom of their library") && logEntry.plainText().contains("random order")));
        }

        @Test
        @DisplayName("Selected cards go to battlefield and remaining cards are exiled")
        void selectedToBattlefieldRestToExile() {
            Card dino = createCard("Colossal Dreadmaw", CardType.CREATURE);
            Card land = createCard("Forest", CardType.LAND);
            Card instant = createCard("Shock", CardType.INSTANT);

            List<Card> allCards = List.of(dino, land, instant);
            Set<UUID> validIds = Set.of(dino.getId());

            gd.interaction.beginInteraction(new com.github.laxika.magicalvibes.model.PendingInteraction.LibraryRevealChoice(
                    player1Id, new ArrayList<>(allCards), new ArrayList<>(validIds),
                    false, false, false, false, true, 0, null, validIds.size(), "Choose."));
            when(battlefieldEntryService.snapshotEnterTappedTypes(gd)).thenReturn(Set.of());

            service.handleLibraryRevealChoice(gd, player1, List.of(dino.getId()));

            verify(battlefieldEntryService).putPermanentOntoBattlefield(eq(gd), eq(player1Id), any(), any());
            verify(exileService).exileCard(gd, player1Id, land);
            verify(exileService).exileCard(gd, player1Id, instant);
            verify(graveyardService, never()).addCardToGraveyard(any(), any(), any());
        }
    }

    @Test
    @DisplayName("handleLibraryRevealChoice orders non-random battlefield leftovers on the bottom")
    void selectedToBattlefieldRestToOrderedBottom() {
        Card dino = createCard("Colossal Dreadmaw", CardType.CREATURE);
        Card land = createCard("Forest", CardType.LAND);
        Card instant = createCard("Shock", CardType.INSTANT);
        List<Card> allCards = List.of(dino, land, instant);

        gd.interaction.beginInteraction(new PendingInteraction.LibraryRevealChoice(
                player1Id, new ArrayList<>(allCards), List.of(dino.getId()),
                false, false, true, false, false, 0, null, 1, "Choose."));
        when(battlefieldEntryService.snapshotEnterTappedTypes(gd)).thenReturn(Set.of());

        service.handleLibraryRevealChoice(gd, player1, List.of(dino.getId()));

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.cards()).containsExactly(land, instant);
        assertThat(reorder.toBottom()).isTrue();
        assertThat(gd.playerDecks.get(player1Id)).isEmpty();
        verify(gameLogService, never()).append(eq(gd), argThat((GameLogEntry logEntry) ->
                logEntry.plainText().contains("Library is shuffled")));
    }

    // =========================================================================
    // handleLibraryCardChosen — resuming effects queued after the search
    // =========================================================================

    @Nested
    @DisplayName("handleLibraryCardChosen resuming effects after the search")
    class HandleLibraryCardChosenResumesRemainingEffects {

        private StackEntry entryWithTwoEffects() {
            List<CardEffect> effects = List.of(new DrawCardEffect(1), new DrawCardEffect(1));
            return new StackEntry(StackEntryType.ACTIVATED_ABILITY, createCard("Shefet Monitor"),
                    player1Id, "Shefet Monitor's ability", effects);
        }

        @Test
        @DisplayName("Resumes the effect after the search when one is queued (search-then-draw)")
        void resumesEffectQueuedAfterSearch() {
            stubCardViewFactory();
            when(battlefieldEntryService.snapshotEnterTappedTypes(gd)).thenReturn(Set.of());

            Card plains = createBasicLand("Plains");
            gd.playerDecks.get(player1Id).add(plains);
            beginBasicLandBattlefieldSearch(player1Id, List.of(plains));

            // The reflexive land search is index 0 of the effect list; the cycling draw at index 1
            // has not resolved yet — the search interrupted resolution here.
            StackEntry entry = entryWithTwoEffects();
            gd.pendingEffectResolutionEntry = entry;
            gd.pendingEffectResolutionIndex = 1;

            service.handleLibraryCardChosen(gd, player1, 0);

            verify(effectResolutionService).resolveEffectsFrom(gd, entry, 1);
            verify(inputCompletionService).processMayAbilitiesThenAutoPassPreservingPriority(gd);
        }

        @Test
        @DisplayName("Search as last effect still drains the parked entry via resolveEffectsFrom")
        void searchAsLastEffectStillDrainsParkedEntry() {
            stubCardViewFactory();
            when(battlefieldEntryService.snapshotEnterTappedTypes(gd)).thenReturn(Set.of());

            Card plains = createBasicLand("Plains");
            gd.playerDecks.get(player1Id).add(plains);
            beginBasicLandBattlefieldSearch(player1Id, List.of(plains));

            // Resume index is past the end of the (two-effect) list. The past-the-end drain in
            // resolveEffectsFrom is what clears the parked entry and releases the deferred
            // player-loss check — skipping it leaves the entry dangling (fuzz invariant).
            StackEntry entry = entryWithTwoEffects();
            gd.pendingEffectResolutionEntry = entry;
            gd.pendingEffectResolutionIndex = 2;

            service.handleLibraryCardChosen(gd, player1, 0);

            verify(effectResolutionService).resolveEffectsFrom(gd, entry, 2);
            verify(inputCompletionService).processMayAbilitiesThenAutoPassPreservingPriority(gd);
        }

        @Test
        @DisplayName("Search-to-top completion resumes the parked entry's trailing effects")
        void searchToTopResumesParkedEntry() {
            Card plains = createBasicLand("Plains");
            gd.playerDecks.get(player1Id).add(plains);
            LibrarySearchParams params = LibrarySearchParams.builder(player1Id, List.of(plains))
                    .canFailToFind(true)
                    .destination(LibrarySearchDestination.TOP_OF_LIBRARY)
                    .build();
            gd.interaction.beginInteraction(new PendingInteraction.LibrarySearch(
                    params, "Search your library for a card to put on top.", true));

            // Cruel Tutor shape: the trailing "you lose 2 life" waits at index 1.
            StackEntry entry = entryWithTwoEffects();
            gd.pendingEffectResolutionEntry = entry;
            gd.pendingEffectResolutionIndex = 1;

            service.handleLibraryCardChosen(gd, player1, 0);

            verify(effectResolutionService).resolveEffectsFrom(gd, entry, 1);
            verify(inputCompletionService).processMayAbilitiesThenAutoPassPreservingPriority(gd);
        }
    }
}
