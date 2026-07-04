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
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.CantSearchLibrariesEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentMaySearchLibraryForBasicLandToBattlefieldTappedEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.normalfx.EachOpponentMaySearchLibraryForBasicLandToBattlefieldTappedEffectHandler;
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
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EachOpponentMaySearchLibraryForBasicLandToBattlefieldTappedEffectHandlerTest {

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
    private EachOpponentMaySearchLibraryForBasicLandToBattlefieldTappedEffectHandler eachOpponentMaySearchBasicLandHandler;

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
        eachOpponentMaySearchBasicLandHandler = new EachOpponentMaySearchLibraryForBasicLandToBattlefieldTappedEffectHandler(support);

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

    private EachOpponentMaySearchLibraryForBasicLandToBattlefieldTappedEffect effect;

            @BeforeEach
            void init() {
                effect = new EachOpponentMaySearchLibraryForBasicLandToBattlefieldTappedEffect();
            }

            private StackEntry entry() {
                return new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Old-Growth Dryads"),
                        player1Id, "Old-Growth Dryads", List.of(effect), 0, null, null);
            }

            @Test
            @DisplayName("Queues only opponents, not controller")
            void queuesOnlyOpponents() {
                stubCardViewFactory();
                gd.playerDecks.get(player1Id).add(createBasicLand("Plains"));
                gd.playerDecks.get(player2Id).add(createBasicLand("Forest"));

                eachOpponentMaySearchBasicLandHandler.resolve(gd, entry(), effect);

                // Only opponent (player2) should be prompted, not the controller (player1)
                assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
                assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId()).isEqualTo(player2Id);
                assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                        .params().followUp().remainingEachPlayerBasicLandSearches()).isEmpty();
            }

            @Test
            @DisplayName("Sets tapped flag to true")
            void setsTappedFlag() {
                stubCardViewFactory();
                gd.playerDecks.get(player2Id).add(createBasicLand("Plains"));

                eachOpponentMaySearchBasicLandHandler.resolve(gd, entry(), effect);

                assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                        .params().followUp().eachPlayerSearchTapped()).isTrue();
                assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                        .isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
            }

            @Test
            @DisplayName("No search when opponent has no basic lands")
            void noSearchWhenNoBasicLands() {
                gd.playerDecks.get(player2Id).add(createCard("Grizzly Bears", CardType.CREATURE));

                eachOpponentMaySearchBasicLandHandler.resolve(gd, entry(), effect);

                assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
            }
}
