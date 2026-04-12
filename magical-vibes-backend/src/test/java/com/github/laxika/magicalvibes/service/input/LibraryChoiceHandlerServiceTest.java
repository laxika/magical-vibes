package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.state.StateBasedActionService;
import com.github.laxika.magicalvibes.service.WarpWorldService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
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
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibraryChoiceHandlerServiceTest {

    @Mock private SessionManager sessionManager;
    @Mock private GameQueryService gameQueryService;
    @Mock private GraveyardService graveyardService;
    @Mock private WarpWorldService warpWorldService;
    @Mock private BattlefieldEntryService battlefieldEntryService;
    @Mock private LegendRuleService legendRuleService;
    @Mock private StateBasedActionService stateBasedActionService;
    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private CardViewFactory cardViewFactory;
    @Mock private TurnProgressionService turnProgressionService;
    @Mock private PlayerInputService playerInputService;
    @Mock private EffectResolutionService effectResolutionService;
    @Mock private ExileService exileService;

    @InjectMocks
    private LibraryChoiceHandlerService service;

    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
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
        LibrarySearchParams params = LibrarySearchParams.builder(playerId, searchCards)
                .reveals(false)
                .canFailToFind(true)
                .destination(LibrarySearchDestination.BATTLEFIELD)
                .build();
        gd.interaction.beginLibrarySearch(params);
    }

    // =========================================================================
    // handleLibraryCardChosen — pendingEachPlayerBasicLandSearchQueue processing
    // =========================================================================

    @Nested
    @DisplayName("handleLibraryCardChosen with pendingEachPlayerBasicLandSearchQueue")
    class HandleLibraryCardChosenWithEachPlayerQueue {

        @Test
        @DisplayName("After successful choice, starts next player's search from queue")
        void successfulChoiceStartsNextPlayerSearch() {
            stubCardViewFactory();

            // Player1 is currently searching; player2 is queued
            Card plains = createBasicLand("Plains");
            gd.playerDecks.get(player1Id).add(plains);
            beginBasicLandBattlefieldSearch(player1Id, List.of(plains));

            Card forest = createBasicLand("Forest");
            gd.playerDecks.get(player2Id).add(forest);
            gd.pendingEachPlayerBasicLandSearchQueue.add(player2Id);


            // Player1 picks index 0
            service.handleLibraryCardChosen(gd, player1, 0);

            // Player2 should now be prompted to search
            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
            assertThat(gd.interaction.librarySearch().playerId()).isEqualTo(player2Id);
            assertThat(gd.pendingEachPlayerBasicLandSearchQueue).isEmpty();
        }

        @Test
        @DisplayName("After fail-to-find, starts next player's search from queue")
        void failToFindStartsNextPlayerSearch() {
            stubCardViewFactory();

            // Player1 is currently searching; player2 is queued
            Card plains = createBasicLand("Plains");
            beginBasicLandBattlefieldSearch(player1Id, List.of(plains));
            gd.playerDecks.get(player1Id).add(plains);

            Card forest = createBasicLand("Forest");
            gd.playerDecks.get(player2Id).add(forest);
            gd.pendingEachPlayerBasicLandSearchQueue.add(player2Id);

            // Player1 declines (-1)
            service.handleLibraryCardChosen(gd, player1, -1);

            // Player2 should now be prompted to search
            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
            assertThat(gd.interaction.librarySearch().playerId()).isEqualTo(player2Id);
        }

        @Test
        @DisplayName("After last player in queue completes, resolves auto-pass")
        void lastPlayerCompletesResolvesAutoPass() {
            // Player2 is searching; queue is empty
            Card forest = createBasicLand("Forest");
            gd.playerDecks.get(player2Id).add(forest);
            beginBasicLandBattlefieldSearch(player2Id, List.of(forest));


            // Queue is empty — no more pending searches
            assertThat(gd.pendingEachPlayerBasicLandSearchQueue).isEmpty();

            service.handleLibraryCardChosen(gd, player2, 0);

            // Should resolve auto-pass since queue is empty
            verify(turnProgressionService).resolveAutoPass(gd);
        }

        @Test
        @DisplayName("Skips queued player with no basic lands and tries next")
        void skipsQueuedPlayerWithNoBasicLands() {
            stubCardViewFactory();

            // Player1 is currently searching
            Card plains = createBasicLand("Plains");
            gd.playerDecks.get(player1Id).add(plains);
            beginBasicLandBattlefieldSearch(player1Id, List.of(plains));

            // Queue: player2 has no basic lands
            gd.playerDecks.get(player2Id).add(createCard("Grizzly Bears", CardType.CREATURE));
            gd.pendingEachPlayerBasicLandSearchQueue.add(player2Id);


            service.handleLibraryCardChosen(gd, player1, 0);

            // Player2 was skipped (no basic lands), auto-pass called
            assertThat(gd.pendingEachPlayerBasicLandSearchQueue).isEmpty();
            verify(turnProgressionService).resolveAutoPass(gd);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("Player2") && msg.contains("finds no basic land cards")));
        }

        @Test
        @DisplayName("Skips queued player with empty library and tries next")
        void skipsQueuedPlayerWithEmptyLibrary() {
            stubCardViewFactory();

            // Player1 is currently searching
            Card plains = createBasicLand("Plains");
            gd.playerDecks.get(player1Id).add(plains);
            beginBasicLandBattlefieldSearch(player1Id, List.of(plains));

            // Queue: player2 has empty library (already empty from setUp)
            gd.pendingEachPlayerBasicLandSearchQueue.add(player2Id);


            service.handleLibraryCardChosen(gd, player1, 0);

            // Player2 was skipped (empty library), auto-pass called
            assertThat(gd.pendingEachPlayerBasicLandSearchQueue).isEmpty();
            verify(turnProgressionService).resolveAutoPass(gd);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("Player2") && msg.contains("it is empty")));
        }

        @Test
        @DisplayName("Queue is not processed when it is empty")
        void emptyQueueDoesNothing() {
            // Player1 searches with empty queue
            Card plains = createBasicLand("Plains");
            gd.playerDecks.get(player1Id).add(plains);
            beginBasicLandBattlefieldSearch(player1Id, List.of(plains));


            service.handleLibraryCardChosen(gd, player1, 0);

            // No further library search, just auto-pass
            verify(turnProgressionService).resolveAutoPass(gd);
        }

        @Test
        @DisplayName("Search only presents basic land cards to queued player")
        void searchOnlyPresentsBasicLandsToQueuedPlayer() {
            stubCardViewFactory();

            // Player1 is currently searching
            Card plains = createBasicLand("Plains");
            gd.playerDecks.get(player1Id).add(plains);
            beginBasicLandBattlefieldSearch(player1Id, List.of(plains));

            // Queue: player2 has mixed library
            gd.playerDecks.get(player2Id).addAll(List.of(
                    createBasicLand("Forest"),
                    createBasicLand("Island"),
                    createCard("Grizzly Bears", CardType.CREATURE),
                    createCard("Ghost Quarter", CardType.LAND) // nonbasic
            ));
            gd.pendingEachPlayerBasicLandSearchQueue.add(player2Id);


            service.handleLibraryCardChosen(gd, player1, 0);

            // Player2 should only see basic land cards
            assertThat(gd.interaction.librarySearch().cards()).hasSize(2);
            assertThat(gd.interaction.librarySearch().cards())
                    .allMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC));
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

            gd.interaction.beginLibraryRevealChoiceRandomBottom(player1Id, new ArrayList<>(allCards), new java.util.HashSet<>(validIds));
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

            gd.interaction.beginLibraryRevealChoiceRandomBottom(player1Id, new ArrayList<>(allCards), new java.util.HashSet<>(validIds));

            service.handleLibraryRevealChoice(gd, player1, List.of());

            // Nothing put onto battlefield
            verify(battlefieldEntryService, never()).putPermanentOntoBattlefield(any(), any(), any());

            // All cards on bottom of library
            assertThat(gd.playerDecks.get(player1Id)).hasSize(2);
            verify(graveyardService, never()).addCardToGraveyard(any(), any(), any());
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(s ->
                    s.contains("bottom of their library") && s.contains("random order")));
        }
    }
}
