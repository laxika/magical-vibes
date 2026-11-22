package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
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
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibrarySearchSupportEachPlayerBasicLandTest {

    @Mock
    private DrawService drawService;
    @Mock
    private GameLogService gameLogService;
    @Mock
    private SessionManager sessionManager;
    @Mock
    private CardViewFactory cardViewFactory;
    @Mock
    private GameQueryService gameQueryService;
    private LibrarySearchSupport support;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

    @BeforeEach
    void setUp() {
        lenient().when(gameQueryService.withQueryScope(any(), any()))
                .thenAnswer(invocation -> invocation.<Supplier<?>>getArgument(1).get());
        support = new LibrarySearchSupport(gameLogService,
                InteractionRegistryTestSupport.registryFor(sessionManager, cardViewFactory, gameLogService),
                gameQueryService);

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
        lenient().when(gameQueryService.cardHasSupertype(any(), any(), any(), any())).thenAnswer(invocation ->
                invocation.<Card>getArgument(0).getSupertypes().contains(invocation.<CardSupertype>getArgument(1)));

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


    @Test
            @DisplayName("Returns false when queue is empty")
            void returnsFalseWhenQueueEmpty() {
                boolean result = support.startNextEachPlayerBasicLandSearch(gd,
                        LibrarySearchFollowUp.eachPlayerBasicLand(List.of(), false));

                assertThat(result).isFalse();
            }

            @Test
            @DisplayName("Starts library search for next player in queue")
            void startsSearchForNextPlayer() {
                stubCardViewFactory();
                gd.playerDecks.get(player2Id).add(createBasicLand("Plains"));

                boolean result = support.startNextEachPlayerBasicLandSearch(gd,
                        LibrarySearchFollowUp.eachPlayerBasicLand(List.of(player2Id), false));

                assertThat(result).isTrue();
                assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
                assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId()).isEqualTo(player2Id);
                assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                        .params().followUp().remainingEachPlayerBasicLandSearches()).isEmpty();
            }

            @Test
            @DisplayName("A mandatory search skips a player with no basic lands and tries the next")
            void skipsPlayerWithNoBasicLandsWhenDequeuing() {
                stubCardViewFactory();
                gd.playerDecks.get(player1Id).add(createCard("Grizzly Bears", CardType.CREATURE));
                gd.playerDecks.get(player2Id).add(createBasicLand("Forest"));

                boolean result = support.startNextEachPlayerBasicLandSearch(gd,
                        LibrarySearchFollowUp.eachPlayerBasicLand(List.of(player1Id, player2Id), false), false);

                assertThat(result).isTrue();
                assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId()).isEqualTo(player2Id);
                assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                        .params().followUp().remainingEachPlayerBasicLandSearches()).isEmpty();
            }

            @Test
            @DisplayName("A mandatory search returns false when all queued players lack basic lands")
            void returnsFalseWhenAllPlayersLackBasicLands() {
                gd.playerDecks.get(player1Id).add(createCard("Grizzly Bears", CardType.CREATURE));
                gd.playerDecks.get(player2Id).add(createCard("Grizzly Bears", CardType.CREATURE));

                boolean result = support.startNextEachPlayerBasicLandSearch(gd,
                        LibrarySearchFollowUp.eachPlayerBasicLand(List.of(player1Id, player2Id), false), false);

                assertThat(result).isFalse();
            }

            @Test
            @DisplayName("Uses BATTLEFIELD_TAPPED destination when tapped flag is set")
            void usesBattlefieldTappedDestination() {
                stubCardViewFactory();
                gd.playerDecks.get(player2Id).add(createBasicLand("Plains"));

                boolean result = support.startNextEachPlayerBasicLandSearch(gd,
                        LibrarySearchFollowUp.eachPlayerBasicLand(List.of(player2Id), true));

                assertThat(result).isTrue();
                assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                        .isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
            }

            @Test
            @DisplayName("Uses BATTLEFIELD destination when tapped flag is false")
            void usesBattlefieldDestination() {
                stubCardViewFactory();
                gd.playerDecks.get(player2Id).add(createBasicLand("Plains"));

                boolean result = support.startNextEachPlayerBasicLandSearch(gd,
                        LibrarySearchFollowUp.eachPlayerBasicLand(List.of(player2Id), false));

                assertThat(result).isTrue();
                assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                        .isEqualTo(LibrarySearchDestination.BATTLEFIELD);
            }
}
