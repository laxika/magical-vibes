package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.CantSearchLibrariesEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetAndEachPlayerSearchesBasicLandToBattlefieldEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.normalfx.DestroyTargetAndEachPlayerSearchesBasicLandToBattlefieldEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.LibrarySearchSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DestroyTargetAndEachPlayerSearchesBasicLandToBattlefieldEffectHandlerTest {

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
    private LibrarySearchSupport support;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private DestroyTargetAndEachPlayerSearchesBasicLandToBattlefieldEffectHandler destroyTargetAndEachPlayerSearchesBasicLandHandler;

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
        destroyTargetAndEachPlayerSearchesBasicLandHandler = new DestroyTargetAndEachPlayerSearchesBasicLandToBattlefieldEffectHandler(permanentRemovalService, gameQueryService, gameBroadcastService, support);

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

    private DestroyTargetAndEachPlayerSearchesBasicLandToBattlefieldEffect effect;

            @BeforeEach
            void init() {
                effect = new DestroyTargetAndEachPlayerSearchesBasicLandToBattlefieldEffect();
            }

            private StackEntry entryWithTarget(UUID targetId) {
                return new StackEntry(StackEntryType.ACTIVATED_ABILITY, createCard("Field of Ruin"),
                        player1Id, "Field of Ruin", List.of(effect), 0, targetId, null);
            }

            @Test
            @DisplayName("Returns without action when target permanent not found")
            void returnsWhenTargetNotFound() {
                UUID targetId = UUID.randomUUID();
                StackEntry entry = entryWithTarget(targetId);

                when(gameQueryService.findPermanentById(gd, targetId)).thenReturn(null);

                destroyTargetAndEachPlayerSearchesBasicLandHandler.resolve(gd, entry, effect);

                verify(permanentRemovalService, never()).tryDestroyPermanent(any(), any(), anyBoolean());
                assertThat(gd.pendingEachPlayerBasicLandSearchQueue).isEmpty();
            }

            @Test
            @DisplayName("Destroys target permanent and logs destruction")
            void destroysTargetPermanent() {
                Card landCard = createCard("Ghost Quarter", CardType.LAND);
                Permanent target = new Permanent(landCard);
                StackEntry entry = entryWithTarget(target.getId());

                when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
                when(permanentRemovalService.tryDestroyPermanent(gd, target, false)).thenReturn(true);

                destroyTargetAndEachPlayerSearchesBasicLandHandler.resolve(gd, entry, effect);

                verify(permanentRemovalService).tryDestroyPermanent(gd, target, false);
                verify(gameBroadcastService).logAndBroadcast(eq(gd), eq("Ghost Quarter is destroyed."));
            }

            @Test
            @DisplayName("Builds APNAP queue with active player first")
            void buildsApnapQueue() {
                Card landCard = createCard("Ghost Quarter", CardType.LAND);
                Permanent target = new Permanent(landCard);
                StackEntry entry = entryWithTarget(target.getId());

                when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
                when(permanentRemovalService.tryDestroyPermanent(gd, target, false)).thenReturn(true);
                stubCardViewFactory();

                // active player is player1Id; put basic lands in both libraries
                gd.playerDecks.get(player1Id).add(createBasicLand("Plains"));
                gd.playerDecks.get(player2Id).add(createBasicLand("Forest"));

                destroyTargetAndEachPlayerSearchesBasicLandHandler.resolve(gd, entry, effect);

                // Active player (player1) should be prompted first
                assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
                assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId()).isEqualTo(player1Id);
                // Player2 should be in the pending queue
                assertThat(gd.pendingEachPlayerBasicLandSearchQueue).containsExactly(player2Id);
            }

            @Test
            @DisplayName("Search only shows basic land cards from library")
            void searchOnlyShowsBasicLands() {
                Card landCard = createCard("Ghost Quarter", CardType.LAND);
                Permanent target = new Permanent(landCard);
                StackEntry entry = entryWithTarget(target.getId());

                when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
                when(permanentRemovalService.tryDestroyPermanent(gd, target, false)).thenReturn(true);
                stubCardViewFactory();

                gd.playerDecks.get(player1Id).addAll(List.of(
                        createBasicLand("Plains"),
                        createBasicLand("Island"),
                        createCard("Grizzly Bears", CardType.CREATURE),
                        createCard("Ghost Quarter", CardType.LAND) // nonbasic land
                ));

                destroyTargetAndEachPlayerSearchesBasicLandHandler.resolve(gd, entry, effect);

                assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(2);
                assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                        .allMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC));
            }

            @Test
            @DisplayName("Search destination is BATTLEFIELD")
            void searchDestinationIsBattlefield() {
                Card landCard = createCard("Ghost Quarter", CardType.LAND);
                Permanent target = new Permanent(landCard);
                StackEntry entry = entryWithTarget(target.getId());

                when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
                when(permanentRemovalService.tryDestroyPermanent(gd, target, false)).thenReturn(true);
                stubCardViewFactory();

                gd.playerDecks.get(player1Id).add(createBasicLand("Plains"));

                destroyTargetAndEachPlayerSearchesBasicLandHandler.resolve(gd, entry, effect);

                assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                        .isEqualTo(LibrarySearchDestination.BATTLEFIELD);
            }

            @Test
            @DisplayName("Skips active player with no basic lands and prompts next player")
            void skipsPlayerWithNoBasicLands() {
                Card landCard = createCard("Ghost Quarter", CardType.LAND);
                Permanent target = new Permanent(landCard);
                StackEntry entry = entryWithTarget(target.getId());

                when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
                when(permanentRemovalService.tryDestroyPermanent(gd, target, false)).thenReturn(true);
                stubCardViewFactory();

                // player1 has no basic lands, player2 does
                gd.playerDecks.get(player1Id).add(createCard("Grizzly Bears", CardType.CREATURE));
                gd.playerDecks.get(player2Id).add(createBasicLand("Forest"));

                destroyTargetAndEachPlayerSearchesBasicLandHandler.resolve(gd, entry, effect);

                // Player1 was skipped, player2 is prompted
                assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
                assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId()).isEqualTo(player2Id);
                assertThat(gd.pendingEachPlayerBasicLandSearchQueue).isEmpty();
            }

            @Test
            @DisplayName("Skips active player with empty library and prompts next player")
            void skipsPlayerWithEmptyLibrary() {
                Card landCard = createCard("Ghost Quarter", CardType.LAND);
                Permanent target = new Permanent(landCard);
                StackEntry entry = entryWithTarget(target.getId());

                when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
                when(permanentRemovalService.tryDestroyPermanent(gd, target, false)).thenReturn(true);
                stubCardViewFactory();

                // player1 has empty library, player2 has basic lands
                gd.playerDecks.get(player2Id).add(createBasicLand("Mountain"));

                destroyTargetAndEachPlayerSearchesBasicLandHandler.resolve(gd, entry, effect);

                // Player1 was skipped, player2 is prompted
                assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
                assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId()).isEqualTo(player2Id);
            }

            @Test
            @DisplayName("No search when both players lack basic lands")
            void noSearchWhenNobodyHasBasicLands() {
                Card landCard = createCard("Ghost Quarter", CardType.LAND);
                Permanent target = new Permanent(landCard);
                StackEntry entry = entryWithTarget(target.getId());

                when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
                when(permanentRemovalService.tryDestroyPermanent(gd, target, false)).thenReturn(true);

                // Neither player has basic lands
                gd.playerDecks.get(player1Id).add(createCard("Grizzly Bears", CardType.CREATURE));
                gd.playerDecks.get(player2Id).add(createCard("Grizzly Bears", CardType.CREATURE));

                destroyTargetAndEachPlayerSearchesBasicLandHandler.resolve(gd, entry, effect);

                assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
                assertThat(gd.pendingEachPlayerBasicLandSearchQueue).isEmpty();
            }

            @Test
            @DisplayName("Leonin Arbiter prevents active player's search, paid player still searches")
            void arbiterPreventsActivePlayerPaidPlayerSearches() {
                Card landCard = createCard("Ghost Quarter", CardType.LAND);
                Permanent target = new Permanent(landCard);
                StackEntry entry = entryWithTarget(target.getId());

                when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
                when(permanentRemovalService.tryDestroyPermanent(gd, target, false)).thenReturn(true);
                stubCardViewFactory();

                // Both players have basic lands
                gd.playerDecks.get(player1Id).add(createBasicLand("Plains"));
                gd.playerDecks.get(player2Id).add(createBasicLand("Forest"));

                // Leonin Arbiter on the battlefield
                Permanent arbiter = addArbiterToBattlefield(player2Id);

                // Player2 has paid the Arbiter tax, player1 has not
                gd.paidSearchTaxPermanentIds
                        .computeIfAbsent(player2Id, k -> new HashSet<>())
                        .add(arbiter.getId());

                destroyTargetAndEachPlayerSearchesBasicLandHandler.resolve(gd, entry, effect);

                // Player1's search was prevented (no tax paid), player2 gets to search (tax paid)
                assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
                assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId()).isEqualTo(player2Id);
            }


    @Test
            @DisplayName("Returns false when queue is empty")
            void returnsFalseWhenQueueEmpty() {
                boolean result = support.startNextEachPlayerBasicLandSearch(gd);

                assertThat(result).isFalse();
            }

            @Test
            @DisplayName("Starts library search for next player in queue")
            void startsSearchForNextPlayer() {
                stubCardViewFactory();
                gd.playerDecks.get(player2Id).add(createBasicLand("Plains"));
                gd.pendingEachPlayerBasicLandSearchQueue.add(player2Id);

                boolean result = support.startNextEachPlayerBasicLandSearch(gd);

                assertThat(result).isTrue();
                assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
                assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId()).isEqualTo(player2Id);
                assertThat(gd.pendingEachPlayerBasicLandSearchQueue).isEmpty();
            }

            @Test
            @DisplayName("Skips player with no basic lands and tries next")
            void skipsPlayerWithNoBasicLandsWhenDequeuing() {
                stubCardViewFactory();
                gd.playerDecks.get(player1Id).add(createCard("Grizzly Bears", CardType.CREATURE));
                gd.playerDecks.get(player2Id).add(createBasicLand("Forest"));
                gd.pendingEachPlayerBasicLandSearchQueue.add(player1Id);
                gd.pendingEachPlayerBasicLandSearchQueue.add(player2Id);

                boolean result = support.startNextEachPlayerBasicLandSearch(gd);

                assertThat(result).isTrue();
                assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId()).isEqualTo(player2Id);
                assertThat(gd.pendingEachPlayerBasicLandSearchQueue).isEmpty();
            }

            @Test
            @DisplayName("Returns false when all queued players lack basic lands")
            void returnsFalseWhenAllPlayersLackBasicLands() {
                gd.playerDecks.get(player1Id).add(createCard("Grizzly Bears", CardType.CREATURE));
                gd.playerDecks.get(player2Id).add(createCard("Grizzly Bears", CardType.CREATURE));
                gd.pendingEachPlayerBasicLandSearchQueue.add(player1Id);
                gd.pendingEachPlayerBasicLandSearchQueue.add(player2Id);

                boolean result = support.startNextEachPlayerBasicLandSearch(gd);

                assertThat(result).isFalse();
                assertThat(gd.pendingEachPlayerBasicLandSearchQueue).isEmpty();
            }

            @Test
            @DisplayName("Uses BATTLEFIELD_TAPPED destination when tapped flag is set")
            void usesBattlefieldTappedDestination() {
                stubCardViewFactory();
                gd.playerDecks.get(player2Id).add(createBasicLand("Plains"));
                gd.pendingEachPlayerBasicLandSearchQueue.add(player2Id);
                gd.pendingEachPlayerBasicLandSearchTapped = true;

                boolean result = support.startNextEachPlayerBasicLandSearch(gd);

                assertThat(result).isTrue();
                assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                        .isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
            }

            @Test
            @DisplayName("Uses BATTLEFIELD destination when tapped flag is false")
            void usesBattlefieldDestination() {
                stubCardViewFactory();
                gd.playerDecks.get(player2Id).add(createBasicLand("Plains"));
                gd.pendingEachPlayerBasicLandSearchQueue.add(player2Id);
                gd.pendingEachPlayerBasicLandSearchTapped = false;

                boolean result = support.startNextEachPlayerBasicLandSearch(gd);

                assertThat(result).isTrue();
                assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                        .isEqualTo(LibrarySearchDestination.BATTLEFIELD);
            }
}
