package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.GameLogEntry;

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
import com.github.laxika.magicalvibes.model.effect.HeadGamesEffect;
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
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HeadGamesEffectHandlerTest {

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
            @DisplayName("Empty hand shuffles library and logs")
            void emptyHandShufflesLibrary() {
                // Player2 has empty hand
                HeadGamesEffect effect = new HeadGamesEffect();
                StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Head Games"),
                        player1Id, "Head Games", List.of(effect), 0, player2Id, null);

                headGamesHandler.resolve(gd, entry, effect);

                assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) ->
                        logEntry.plainText().contains("has no cards in hand")));
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

                headGamesHandler.resolve(gd, entry, effect);

                // Player2's hand should be empty (put on library)
                assertThat(gd.playerHands.get(player2Id)).isEmpty();
                // Library should grow by 2 (the 2 cards from hand)
                assertThat(gd.playerDecks.get(player2Id)).hasSize(deckSizeBefore + 2);
                // Player1 should be searching player2's library
                assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
                assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId()).isEqualTo(player1Id);
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) ->
                        logEntry.plainText().contains("puts") && logEntry.plainText().contains("from their hand")));
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

                headGamesHandler.resolve(gd, entry, effect);

                assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) ->
                        logEntry.plainText().contains("3 cards")));
            }
}
