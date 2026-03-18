package com.github.laxika.magicalvibes.service.library;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CantSearchLibrariesEffect;
import com.github.laxika.magicalvibes.model.effect.DistantMemoriesEffect;
import com.github.laxika.magicalvibes.model.effect.HeadGamesEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardTypesToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardTypesToHandEffect;
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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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

@ExtendWith(MockitoExtension.class)
class LibrarySearchResolutionServiceTest {

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
    private PermanentRemovalService permanentRemovalService;

    @Mock
    private PlayerInputService playerInputService;

    @InjectMocks
    private LibrarySearchResolutionService service;

    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

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
        gd.playerExiledCards.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerExiledCards.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.activePlayerId = player1Id;
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

    @Nested
    @DisplayName("resolveSearchLibraryForCardTypesToHand")
    class ResolveSearchLibraryForCardTypesToHand {

        @Test
        @DisplayName("Filters to matching card types and enters LIBRARY_SEARCH")
        void filtersToMatchingTypes() {
            Card plains = createCard("Plains", CardType.LAND);
            Card forest = createCard("Forest", CardType.LAND);
            Card swamp = createCard("Swamp", CardType.LAND);
            Card bears = createCard("Grizzly Bears", CardType.CREATURE);
            gd.playerDecks.get(player1Id).addAll(List.of(plains, forest, swamp, bears));

            CardPredicate filter = mock(CardPredicate.class);
            when(gameQueryService.matchesCardPredicate(eq(plains), eq(filter), isNull())).thenReturn(true);
            when(gameQueryService.matchesCardPredicate(eq(forest), eq(filter), isNull())).thenReturn(true);
            when(gameQueryService.matchesCardPredicate(eq(swamp), eq(filter), isNull())).thenReturn(true);
            when(gameQueryService.matchesCardPredicate(eq(bears), eq(filter), isNull())).thenReturn(false);
            stubCardViewFactory();

            SearchLibraryForCardTypesToHandEffect effect = new SearchLibraryForCardTypesToHandEffect(filter);
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Sylvan Scrying"),
                    player1Id, "Sylvan Scrying", List.of(effect));

            service.resolveSearchLibraryForCardTypesToHand(gd, entry, effect);

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
            assertThat(gd.interaction.librarySearch().playerId()).isEqualTo(player1Id);
            assertThat(gd.interaction.librarySearch().cards()).hasSize(3);
            assertThat(gd.interaction.librarySearch().cards())
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("No matching cards shuffles and logs")
        void noMatchingCardsShuffles() {
            Card bears1 = createCard("Grizzly Bears", CardType.CREATURE);
            Card bears2 = createCard("Grizzly Bears", CardType.CREATURE);
            Card bears3 = createCard("Grizzly Bears", CardType.CREATURE);
            gd.playerDecks.get(player1Id).addAll(List.of(bears1, bears2, bears3));

            CardPredicate filter = mock(CardPredicate.class);
            when(gameQueryService.matchesCardPredicate(any(Card.class), eq(filter), isNull())).thenReturn(false);

            SearchLibraryForCardTypesToHandEffect effect = new SearchLibraryForCardTypesToHandEffect(filter);
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Sylvan Scrying"),
                    player1Id, "Sylvan Scrying", List.of(effect));

            service.resolveSearchLibraryForCardTypesToHand(gd, entry, effect);

            assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("finds no") && msg.contains("Library is shuffled")));
        }

        @Test
        @DisplayName("Empty library logs without crash")
        void emptyLibraryLogs() {
            CardPredicate filter = mock(CardPredicate.class);
            SearchLibraryForCardTypesToHandEffect effect = new SearchLibraryForCardTypesToHandEffect(filter);
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Sylvan Scrying"),
                    player1Id, "Sylvan Scrying", List.of(effect));

            service.resolveSearchLibraryForCardTypesToHand(gd, entry, effect);

            assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("it is empty")));
        }
    }

    // =========================================================================
    // resolveSearchLibraryForCardTypesToBattlefield (via RampantGrowth)
    // =========================================================================

    @Nested
    @DisplayName("resolveSearchLibraryForCardTypesToBattlefield")
    class ResolveSearchLibraryForCardTypesToBattlefield {

        @Test
        @DisplayName("Filters to basic lands and enters LIBRARY_SEARCH")
        void filtersToBasicLands() {
            Card plains = createCard("Plains", CardType.LAND);
            Card forest = createCard("Forest", CardType.LAND);
            Card swamp = createCard("Swamp", CardType.LAND);
            gd.playerDecks.get(player1Id).addAll(List.of(plains, forest, swamp));

            CardPredicate filter = mock(CardPredicate.class);
            when(gameQueryService.matchesCardPredicate(any(Card.class), eq(filter), isNull())).thenReturn(true);
            stubCardViewFactory();

            SearchLibraryForCardTypesToBattlefieldEffect effect =
                    new SearchLibraryForCardTypesToBattlefieldEffect(filter, true);
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Rampant Growth"),
                    player1Id, "Rampant Growth", List.of(effect));

            service.resolveSearchLibraryForCardTypesToBattlefield(gd, entry, effect);

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
            assertThat(gd.interaction.librarySearch().cards())
                    .allMatch(c -> c.getName().equals("Plains")
                            || c.getName().equals("Forest")
                            || c.getName().equals("Swamp"));
        }

        @Test
        @DisplayName("No matching cards shuffles and logs")
        void noMatchingCardsShuffles() {
            Card bears1 = createCard("Grizzly Bears", CardType.CREATURE);
            Card bears2 = createCard("Grizzly Bears", CardType.CREATURE);
            Card bears3 = createCard("Grizzly Bears", CardType.CREATURE);
            gd.playerDecks.get(player1Id).addAll(List.of(bears1, bears2, bears3));

            CardPredicate filter = mock(CardPredicate.class);
            when(gameQueryService.matchesCardPredicate(any(Card.class), eq(filter), isNull())).thenReturn(false);

            SearchLibraryForCardTypesToBattlefieldEffect effect =
                    new SearchLibraryForCardTypesToBattlefieldEffect(filter, true);
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Rampant Growth"),
                    player1Id, "Rampant Growth", List.of(effect));

            service.resolveSearchLibraryForCardTypesToBattlefield(gd, entry, effect);

            assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("finds no") && msg.contains("Library is shuffled")));
        }
    }

    // =========================================================================
    // resolveSearchLibraryForCardToHand (via DiabolicTutor)
    // =========================================================================

    @Nested
    @DisplayName("resolveSearchLibraryForCardToHand")
    class ResolveSearchLibraryForCardToHand {

        @Test
        @DisplayName("Opens all cards in library for search")
        void opensAllCardsForSearch() {
            Card plains = createCard("Plains", CardType.LAND);
            Card forest = createCard("Forest", CardType.LAND);
            Card swamp = createCard("Swamp", CardType.LAND);
            Card bears = createCard("Grizzly Bears", CardType.CREATURE);
            gd.playerDecks.get(player1Id).addAll(List.of(plains, forest, swamp, bears));
            stubCardViewFactory();

            SearchLibraryForCardToHandEffect effect = new SearchLibraryForCardToHandEffect();
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Diabolic Tutor"),
                    player1Id, "Diabolic Tutor", List.of(effect));

            service.resolveSearchLibraryForCardToHand(gd, entry, effect);

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
            assertThat(gd.interaction.librarySearch().playerId()).isEqualTo(player1Id);
            assertThat(gd.interaction.librarySearch().cards()).hasSize(4);
        }

        @Test
        @DisplayName("Empty library logs without entering search")
        void emptyLibraryLogs() {
            SearchLibraryForCardToHandEffect effect = new SearchLibraryForCardToHandEffect();
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Diabolic Tutor"),
                    player1Id, "Diabolic Tutor", List.of(effect));

            service.resolveSearchLibraryForCardToHand(gd, entry, effect);

            assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("it is empty")));
        }
    }

    // =========================================================================
    // resolveDistantMemories (via DistantMemories)
    // =========================================================================

    @Nested
    @DisplayName("resolveDistantMemories")
    class ResolveDistantMemories {

        @Test
        @DisplayName("Sets up exile search with all cards")
        void setsUpExileSearch() {
            Card plains = createCard("Plains", CardType.LAND);
            Card forest = createCard("Forest", CardType.LAND);
            Card swamp = createCard("Swamp", CardType.LAND);
            Card bears = createCard("Grizzly Bears", CardType.CREATURE);
            gd.playerDecks.get(player1Id).addAll(List.of(plains, forest, swamp, bears));
            stubCardViewFactory();

            DistantMemoriesEffect effect = new DistantMemoriesEffect();
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Distant Memories"),
                    player1Id, "Distant Memories", List.of(effect));

            service.resolveDistantMemories(gd, entry);

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
            assertThat(gd.interaction.librarySearch().playerId()).isEqualTo(player1Id);
            assertThat(gd.interaction.librarySearch().cards()).hasSize(4);
        }

        @Test
        @DisplayName("Empty library draws three cards")
        void emptyLibraryDrawsThree() {
            DistantMemoriesEffect effect = new DistantMemoriesEffect();
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Distant Memories"),
                    player1Id, "Distant Memories", List.of(effect));

            service.resolveDistantMemories(gd, entry);

            assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("it is empty")));
            verify(drawService, times(3)).resolveDrawCard(gd, player1Id);
        }
    }

    // =========================================================================
    // resolveHeadGames (via HeadGames)
    // =========================================================================

    @Nested
    @DisplayName("resolveHeadGames")
    class ResolveHeadGames {

        @Test
        @DisplayName("Empty hand shuffles library and logs")
        void emptyHandShufflesLibrary() {
            // Player2 has empty hand
            HeadGamesEffect effect = new HeadGamesEffect();
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Head Games"),
                    player1Id, "Head Games", List.of(effect), 0, player2Id, null);

            service.resolveHeadGames(gd, entry, effect);

            assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("has no cards in hand")));
        }

        @Test
        @DisplayName("Target hand cards are put on top of library then search begins")
        void handPutOnLibraryThenSearchBegins() {
            Card bear1 = createCard("Grizzly Bears");
            Card bear2 = createCard("Grizzly Bears");
            gd.playerHands.get(player2Id).addAll(List.of(bear1, bear2));
            stubCardViewFactory();

            int deckSizeBefore = gd.playerDecks.get(player2Id).size();

            HeadGamesEffect effect = new HeadGamesEffect();
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Head Games"),
                    player1Id, "Head Games", List.of(effect), 0, player2Id, null);

            service.resolveHeadGames(gd, entry, effect);

            // Player2's hand should be empty (put on library)
            assertThat(gd.playerHands.get(player2Id)).isEmpty();
            // Library should grow by 2 (the 2 cards from hand)
            assertThat(gd.playerDecks.get(player2Id)).hasSize(deckSizeBefore + 2);
            // Player1 should be searching player2's library
            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
            assertThat(gd.interaction.librarySearch().playerId()).isEqualTo(player1Id);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("puts") && msg.contains("from their hand")));
        }

        @Test
        @DisplayName("Caster searches for exactly as many cards as target's hand size")
        void searchesForHandSizeCards() {
            gd.playerHands.get(player2Id).addAll(List.of(
                    createCard("Grizzly Bears"), createCard("Grizzly Bears"), createCard("Grizzly Bears")));
            stubCardViewFactory();

            HeadGamesEffect effect = new HeadGamesEffect();
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Head Games"),
                    player1Id, "Head Games", List.of(effect), 0, player2Id, null);

            service.resolveHeadGames(gd, entry, effect);

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("3 cards")));
        }
    }

    // =========================================================================
    // checkSearchRestriction (Leonin Arbiter with various search types)
    // =========================================================================

    @Nested
    @DisplayName("checkSearchRestriction")
    class CheckSearchRestriction {

        @Test
        @DisplayName("Leonin Arbiter prevents SylvanScrying search without payment")
        void preventsSearchWithoutPayment() {
            addArbiterToBattlefield(player2Id);

            Card plains = createCard("Plains", CardType.LAND);
            gd.playerDecks.get(player1Id).add(plains);

            CardPredicate filter = mock(CardPredicate.class);
            SearchLibraryForCardTypesToHandEffect effect = new SearchLibraryForCardTypesToHandEffect(filter);
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Sylvan Scrying"),
                    player1Id, "Sylvan Scrying", List.of(effect));

            service.resolveSearchLibraryForCardTypesToHand(gd, entry, effect);

            assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("prevented by Leonin Arbiter")));
        }

        @Test
        @DisplayName("Search proceeds when no Leonin Arbiter is present")
        void searchProceedsWithoutArbiter() {
            Card plains = createCard("Plains", CardType.LAND);
            gd.playerDecks.get(player1Id).add(plains);

            CardPredicate filter = mock(CardPredicate.class);
            when(gameQueryService.matchesCardPredicate(any(Card.class), eq(filter), isNull())).thenReturn(true);
            stubCardViewFactory();

            SearchLibraryForCardTypesToHandEffect effect = new SearchLibraryForCardTypesToHandEffect(filter);
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Sylvan Scrying"),
                    player1Id, "Sylvan Scrying", List.of(effect));

            service.resolveSearchLibraryForCardTypesToHand(gd, entry, effect);

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
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

            SearchLibraryForCardToHandEffect effect = new SearchLibraryForCardToHandEffect();
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Diabolic Tutor"),
                    player1Id, "Diabolic Tutor", List.of(effect));

            service.resolveSearchLibraryForCardToHand(gd, entry, effect);

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        }

        @Test
        @DisplayName("Leonin Arbiter prevents RampantGrowth search and shuffles library")
        void preventsRampantGrowthSearch() {
            addArbiterToBattlefield(player2Id);

            Card plains = createCard("Plains", CardType.LAND);
            gd.playerDecks.get(player1Id).add(plains);

            int deckSize = gd.playerDecks.get(player1Id).size();

            CardPredicate filter = mock(CardPredicate.class);
            SearchLibraryForCardTypesToBattlefieldEffect effect =
                    new SearchLibraryForCardTypesToBattlefieldEffect(filter, true);
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Rampant Growth"),
                    player1Id, "Rampant Growth", List.of(effect));

            service.resolveSearchLibraryForCardTypesToBattlefield(gd, entry, effect);

            assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
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

            service.resolveDistantMemories(gd, entry);

            assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
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

            service.resolveHeadGames(gd, entry, effect);

            // Hand was moved to library even though search was prevented
            assertThat(gd.playerHands.get(player2Id)).isEmpty();
            assertThat(gd.playerDecks.get(player2Id)).hasSize(deckSizeBefore + 1);
            // Search was prevented
            assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("prevented by Leonin Arbiter")));
        }
    }
}
