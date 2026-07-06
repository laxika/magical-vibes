package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.CantSearchLibrariesEffect;
import com.github.laxika.magicalvibes.model.effect.DistantMemoriesEffect;
import com.github.laxika.magicalvibes.model.effect.HeadGamesEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardsToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardTypesToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;

@ExtendWith(MockitoExtension.class)
class SearchLibraryForCardsToHandEffectHandlerTest {

    @Mock
    private DrawService drawService;
    @Mock
    private GameBroadcastService gameBroadcastService;
    @Mock
    private SessionManager sessionManager;
    @Mock
    private CardViewFactory cardViewFactory;
    @Mock
    private GameQueryService gameQueryService;
    @Mock
    private PredicateEvaluationService predicateEvaluationService;
    @Mock
    private PermanentRemovalService permanentRemovalService;
    @Mock
    private PlayerInputService playerInputService;
    private LibrarySearchSupport support;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private SearchLibraryForCardsToHandEffectHandler searchLibraryForCardsToHandHandler;
    private SearchLibraryForCardTypesToBattlefieldEffectHandler searchLibraryForCardTypesToBattlefieldHandler;
    private DistantMemoriesEffectHandler distantMemoriesHandler;
    private HeadGamesEffectHandler headGamesHandler;

    @BeforeEach
    void setUp() {
        support = new LibrarySearchSupport(gameBroadcastService,
                InteractionRegistryTestSupport.registryFor(sessionManager, cardViewFactory, gameBroadcastService));

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
        gd.activePlayerId = player1Id;
        searchLibraryForCardsToHandHandler = new SearchLibraryForCardsToHandEffectHandler(gameQueryService, predicateEvaluationService, gameBroadcastService, support);
        searchLibraryForCardTypesToBattlefieldHandler = new SearchLibraryForCardTypesToBattlefieldEffectHandler(gameQueryService, predicateEvaluationService, gameBroadcastService, support);
        distantMemoriesHandler = new DistantMemoriesEffectHandler(drawService, gameBroadcastService, support);
        headGamesHandler = new HeadGamesEffectHandler(gameBroadcastService, support);

    }

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

        private static Card createBasicLand(String name) {
            Card card = createCard(name, CardType.LAND);
            card.setSupertypes(Set.of(CardSupertype.BASIC));
            return card;
        }

        private void stubCardViewFactory() {
            lenient().when(cardViewFactory.create(any(Card.class))).thenReturn(mock(CardView.class));
        }

        private Permanent addArbiterToBattlefield(UUID playerId) {
            Card arbiterCard = createCard("Leonin Arbiter");
            arbiterCard.addEffect(EffectSlot.STATIC, new CantSearchLibrariesEffect());
            Permanent arbiter = new Permanent(arbiterCard);
            gd.playerBattlefields.get(playerId).add(arbiter);
            return arbiter;
        }

        // =========================================================================
        // resolveSearchLibraryForCardTypesToHand (via SylvanScrying)
        // =========================================================================

    @Test
            @DisplayName("Filters to matching card types and enters LIBRARY_SEARCH")
            void filtersToMatchingTypes() {
                Card plains = createCard("Plains", CardType.LAND);
                Card forest = createCard("Forest", CardType.LAND);
                Card swamp = createCard("Swamp", CardType.LAND);
                Card bears = createCard("Grizzly Bears", CardType.CREATURE);
                gd.playerDecks.get(player1Id).addAll(List.of(plains, forest, swamp, bears));

                CardPredicate filter = new CardNamedPredicate("Test Filter");
                when(predicateEvaluationService.matchesCardPredicate(eq(plains), eq(filter), isNull())).thenReturn(true);
                when(predicateEvaluationService.matchesCardPredicate(eq(forest), eq(filter), isNull())).thenReturn(true);
                when(predicateEvaluationService.matchesCardPredicate(eq(swamp), eq(filter), isNull())).thenReturn(true);
                when(predicateEvaluationService.matchesCardPredicate(eq(bears), eq(filter), isNull())).thenReturn(false);
                stubCardViewFactory();

                SearchLibraryForCardsToHandEffect effect = new SearchLibraryForCardsToHandEffect(filter);
                StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Sylvan Scrying"),
                        player1Id, "Sylvan Scrying", List.of(effect));

                searchLibraryForCardsToHandHandler.resolve(gd, entry, effect);

                assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
                assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId()).isEqualTo(player1Id);
                assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(3);
                assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                        .noneMatch(c -> c.getName().equals("Grizzly Bears"));
            }

            @Test
            @DisplayName("No matching cards shuffles and logs")
            void noMatchingCardsShuffles() {
                Card bears1 = createCard("Grizzly Bears", CardType.CREATURE);
                Card bears2 = createCard("Grizzly Bears", CardType.CREATURE);
                Card bears3 = createCard("Grizzly Bears", CardType.CREATURE);
                gd.playerDecks.get(player1Id).addAll(List.of(bears1, bears2, bears3));

                CardPredicate filter = new CardNamedPredicate("Test Filter");
                when(predicateEvaluationService.matchesCardPredicate(any(Card.class), eq(filter), isNull())).thenReturn(false);

                SearchLibraryForCardsToHandEffect effect = new SearchLibraryForCardsToHandEffect(filter);
                StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Sylvan Scrying"),
                        player1Id, "Sylvan Scrying", List.of(effect));

                searchLibraryForCardsToHandHandler.resolve(gd, entry, effect);

                assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("finds no") && msg.contains("Library is shuffled")));
            }

            @Test
            @DisplayName("Empty library logs without crash")
            void emptyLibraryLogs() {
                CardPredicate filter = new CardNamedPredicate("Test Filter");
                SearchLibraryForCardsToHandEffect effect = new SearchLibraryForCardsToHandEffect(filter);
                StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Sylvan Scrying"),
                        player1Id, "Sylvan Scrying", List.of(effect));

                searchLibraryForCardsToHandHandler.resolve(gd, entry, effect);

                assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("it is empty")));
            }


    @Test
            @DisplayName("Leonin Arbiter prevents SylvanScrying search without payment")
            void preventsSearchWithoutPayment() {
                addArbiterToBattlefield(player2Id);

                Card plains = createCard("Plains", CardType.LAND);
                gd.playerDecks.get(player1Id).add(plains);

                CardPredicate filter = new CardNamedPredicate("Test Filter");
                SearchLibraryForCardsToHandEffect effect = new SearchLibraryForCardsToHandEffect(filter);
                StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Sylvan Scrying"),
                        player1Id, "Sylvan Scrying", List.of(effect));

                searchLibraryForCardsToHandHandler.resolve(gd, entry, effect);

                assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("prevented by Leonin Arbiter")));
            }

            @Test
            @DisplayName("Search proceeds when no Leonin Arbiter is present")
            void searchProceedsWithoutArbiter() {
                Card plains = createCard("Plains", CardType.LAND);
                gd.playerDecks.get(player1Id).add(plains);

                CardPredicate filter = new CardNamedPredicate("Test Filter");
                when(predicateEvaluationService.matchesCardPredicate(any(Card.class), eq(filter), isNull())).thenReturn(true);
                stubCardViewFactory();

                SearchLibraryForCardsToHandEffect effect = new SearchLibraryForCardsToHandEffect(filter);
                StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Sylvan Scrying"),
                        player1Id, "Sylvan Scrying", List.of(effect));

                searchLibraryForCardsToHandHandler.resolve(gd, entry, effect);

                assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
            }

            @Test
            @DisplayName("Search proceeds after paying Leonin Arbiter tax")
            void searchProceedsAfterPayingTax() {
                Permanent arbiter = addArbiterToBattlefield(player2Id);

                Card plains = createCard("Plains", CardType.LAND);
                gd.playerDecks.get(player1Id).add(plains);

                // Simulate paying the Arbiter tax
                gd.paidSearchTaxPermanentIds
                        .computeIfAbsent(player1Id, k -> new HashSet<>())
                        .add(arbiter.getId());

                stubCardViewFactory();

                SearchLibraryForCardsToHandEffect effect = new SearchLibraryForCardsToHandEffect();
                StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Diabolic Tutor"),
                        player1Id, "Diabolic Tutor", List.of(effect));

                searchLibraryForCardsToHandHandler.resolve(gd, entry, effect);

                assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
            }

            @Test
            @DisplayName("Leonin Arbiter prevents RampantGrowth search and shuffles library")
            void preventsRampantGrowthSearch() {
                addArbiterToBattlefield(player2Id);

                Card plains = createCard("Plains", CardType.LAND);
                gd.playerDecks.get(player1Id).add(plains);

                int deckSize = gd.playerDecks.get(player1Id).size();

                CardPredicate filter = new CardNamedPredicate("Test Filter");
                SearchLibraryForCardTypesToBattlefieldEffect effect =
                        new SearchLibraryForCardTypesToBattlefieldEffect(filter, true);
                StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Rampant Growth"),
                        player1Id, "Rampant Growth", List.of(effect));

                searchLibraryForCardTypesToBattlefieldHandler.resolve(gd, entry, effect);

                assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("prevented by Leonin Arbiter")));
                // Library should be same size (shuffled, not modified)
                assertThat(gd.playerDecks.get(player1Id)).hasSize(deckSize);
            }

            @Test
            @DisplayName("Leonin Arbiter prevents Distant Memories search")
            void preventsDistantMemoriesSearch() {
                addArbiterToBattlefield(player2Id);

                gd.playerDecks.get(player1Id).add(createCard("Plains", CardType.LAND));

                DistantMemoriesEffect effect = new DistantMemoriesEffect();
                StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Distant Memories"),
                        player1Id, "Distant Memories", List.of(effect));

                distantMemoriesHandler.resolve(gd, entry, new DistantMemoriesEffect());

                assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("prevented by Leonin Arbiter")));
            }

            @Test
            @DisplayName("Leonin Arbiter prevents Head Games search but hand still goes to library")
            void preventsHeadGamesSearchButHandMovesToLibrary() {
                addArbiterToBattlefield(player1Id);

                Card bear = createCard("Grizzly Bears");
                gd.playerHands.get(player2Id).add(bear);

                int deckSizeBefore = gd.playerDecks.get(player2Id).size();

                HeadGamesEffect effect = new HeadGamesEffect();
                StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Head Games"),
                        player1Id, "Head Games", List.of(effect), 0, player2Id, null);

                headGamesHandler.resolve(gd, entry, effect);

                // Hand was moved to library even though search was prevented
                assertThat(gd.playerHands.get(player2Id)).isEmpty();
                assertThat(gd.playerDecks.get(player2Id)).hasSize(deckSizeBefore + 1);
                // Search was prevented
                assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("prevented by Leonin Arbiter")));
            }
}
