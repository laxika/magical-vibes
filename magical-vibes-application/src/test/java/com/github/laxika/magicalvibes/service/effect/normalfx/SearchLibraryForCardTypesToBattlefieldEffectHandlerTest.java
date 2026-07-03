package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.CantSearchLibrariesEffect;
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
import com.github.laxika.magicalvibes.service.effect.normalfx.LibrarySearchSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.SearchLibraryForCardTypesToBattlefieldEffectHandler;
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
class SearchLibraryForCardTypesToBattlefieldEffectHandlerTest {

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
    @InjectMocks
    private LibrarySearchSupport support;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private SearchLibraryForCardTypesToBattlefieldEffectHandler searchLibraryForCardTypesToBattlefieldHandler;

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
        gd.activePlayerId = player1Id;
        searchLibraryForCardTypesToBattlefieldHandler = new SearchLibraryForCardTypesToBattlefieldEffectHandler(gameQueryService, predicateEvaluationService, gameBroadcastService, support);

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
            @DisplayName("Filters to basic lands and enters LIBRARY_SEARCH")
            void filtersToBasicLands() {
                Card plains = createCard("Plains", CardType.LAND);
                Card forest = createCard("Forest", CardType.LAND);
                Card swamp = createCard("Swamp", CardType.LAND);
                gd.playerDecks.get(player1Id).addAll(List.of(plains, forest, swamp));

                CardPredicate filter = new CardNamedPredicate("Test Filter");
                when(predicateEvaluationService.matchesCardPredicate(any(Card.class), eq(filter), isNull())).thenReturn(true);
                stubCardViewFactory();

                SearchLibraryForCardTypesToBattlefieldEffect effect =
                        new SearchLibraryForCardTypesToBattlefieldEffect(filter, true);
                StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Rampant Growth"),
                        player1Id, "Rampant Growth", List.of(effect));

                searchLibraryForCardTypesToBattlefieldHandler.resolve(gd, entry, effect);

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

                CardPredicate filter = new CardNamedPredicate("Test Filter");
                when(predicateEvaluationService.matchesCardPredicate(any(Card.class), eq(filter), isNull())).thenReturn(false);

                SearchLibraryForCardTypesToBattlefieldEffect effect =
                        new SearchLibraryForCardTypesToBattlefieldEffect(filter, true);
                StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Rampant Growth"),
                        player1Id, "Rampant Growth", List.of(effect));

                searchLibraryForCardTypesToBattlefieldHandler.resolve(gd, entry, effect);

                assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("finds no") && msg.contains("Library is shuffled")));
            }
}
