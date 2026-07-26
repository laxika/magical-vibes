package com.github.laxika.magicalvibes.service.event;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.event.GameEventAudience;
import com.github.laxika.magicalvibes.model.event.GameEventBatch;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.model.event.GameEventKind;
import com.github.laxika.magicalvibes.networking.Connection;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.RevealHandMessage;
import com.github.laxika.magicalvibes.networking.message.RevealLibraryTopMessage;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.networking.service.GameLogViewFactory;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameMessageTransport;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.GameViewProjectionFactory;
import com.github.laxika.magicalvibes.service.PrivateInformationProjectionFactory;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CardRevealEventVisibilityTest {

    private UUID ownerId;
    private UUID controllerId;
    private UUID aiOpponentId;
    private UUID spectatorId;
    private GameData gameData;
    private RecordingSessionManager sessions;
    private GameLogService gameLogService;
    private GameMutationCoordinator coordinator;
    private CardRevealService reveals;
    private List<GameEventBatch> batches;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
        controllerId = UUID.randomUUID();
        aiOpponentId = UUID.randomUUID();
        spectatorId = UUID.randomUUID();

        gameData = new GameData(UUID.randomUUID(), "reveal-visibility", ownerId, "Owner");
        addPlayer(ownerId, "Owner");
        addPlayer(controllerId, "Controller");
        addPlayer(aiOpponentId, "AI opponent");
        gameData.playerIdToName.put(spectatorId, "Spectator");

        GameRegistry registry = new GameRegistry();
        registry.register(gameData);
        sessions = new RecordingSessionManager();
        GameEventProjectionSubscriber projection = new GameEventProjectionSubscriber(
                registry,
                mock(GameViewProjectionFactory.class),
                new PrivateInformationProjectionFactory(new CardViewFactory()),
                new InteractionPromptProjectionRegistry(new CardViewFactory()),
                mock(GameLogViewFactory.class),
                new GameMessageTransport(sessions));
        batches = new ArrayList<>();
        coordinator = new GameMutationCoordinator(
                new GameEventDispatcher(List.of(projection, batches::add)));
        gameLogService = mock(GameLogService.class);
        reveals = new CardRevealService(
                gameLogService, mock(GameQueryService.class), coordinator);
    }

    @Test
    void nonemptyPrivateHandLookReachesOnlyTheControllerAndDoesNotImplyStateProjection() {
        Card hidden = card("Hidden hand card");
        gameData.playerHands.get(ownerId).add(hidden);

        coordinator.mutate(gameData, () ->
                reveals.lookAtHand(gameData, controllerId, ownerId));

        assertThat(sessions.deliveries)
                .singleElement()
                .satisfies(delivery -> {
                    assertThat(delivery.playerId()).isEqualTo(controllerId);
                    assertThat(delivery.message()).isInstanceOfSatisfying(
                            RevealHandMessage.class,
                            message -> assertThat(message.cards())
                                    .extracting(card -> card.name())
                                    .containsExactly("Hidden hand card"));
                });
        assertThat(messagesFor(ownerId)).isEmpty();
        assertThat(messagesFor(aiOpponentId)).isEmpty();
        assertThat(messagesFor(spectatorId)).isEmpty();
        assertPrivateRevealBatch(ownerId, controllerId, 1);

        ArgumentCaptor<GameLogEntry> logEntry = ArgumentCaptor.forClass(GameLogEntry.class);
        verify(gameLogService).append(eq(gameData), logEntry.capture());
        assertThat(logEntry.getValue().plainText())
                .contains("Controller looks at Owner's hand")
                .doesNotContain("Hidden hand card");
    }

    @Test
    void emptyPrivateHandLookStillProjectsTheExistingEmptyHandShapeOnlyToTheController() {
        coordinator.mutate(gameData, () ->
                reveals.lookAtHand(gameData, controllerId, ownerId));

        assertThat(messagesFor(controllerId))
                .singleElement()
                .isInstanceOfSatisfying(
                        RevealHandMessage.class,
                        message -> assertThat(message.cards()).isEmpty());
        assertThat(messagesFor(ownerId)).isEmpty();
        assertThat(messagesFor(aiOpponentId)).isEmpty();
        assertPrivateRevealBatch(ownerId, controllerId, 0);
    }

    @Test
    void rulesPublicHandRevealNamesEverySeatedPlayerIncludingAiButNotSpectators() {
        Card revealed = card("Publicly revealed card");
        gameData.playerHands.get(ownerId).add(revealed);

        coordinator.mutate(gameData, () ->
                reveals.revealHandToAllPlayers(gameData, ownerId));

        assertThat(sessions.deliveries)
                .extracting(Delivery::playerId)
                .containsExactly(ownerId, controllerId, aiOpponentId);
        assertThat(sessions.deliveries)
                .extracting(Delivery::message)
                .allSatisfy(message -> assertThat(message)
                        .isInstanceOfSatisfying(
                                RevealHandMessage.class,
                                reveal -> assertThat(reveal.cards())
                                        .extracting(card -> card.name())
                                        .containsExactly("Publicly revealed card")));
        assertThat(messagesFor(spectatorId)).isEmpty();

        GameEventAudience audience = batches.getLast().events().getFirst().audience();
        assertThat(audience.visibility()).isEqualTo(GameEventAudience.Visibility.PRIVATE);
        assertThat(audience.playerIds())
                .containsExactlyInAnyOrder(ownerId, controllerId, aiOpponentId)
                .doesNotContain(spectatorId);
    }

    @Test
    void emptyRulesPublicHandRevealProjectsAnEmptyHandToEverySeatedPlayer() {
        coordinator.mutate(gameData, () ->
                reveals.revealHandToAllPlayers(gameData, ownerId));

        assertThat(sessions.deliveries)
                .hasSize(3)
                .allSatisfy(delivery -> assertThat(delivery.message())
                        .isInstanceOfSatisfying(
                                RevealHandMessage.class,
                                message -> assertThat(message.cards()).isEmpty()));
    }

    @Test
    void privateLibraryLookUsesLibraryShapeForNonemptyAndEmptyLibraries() {
        Card top = card("Secret top card");
        gameData.playerDecks.get(ownerId).add(top);

        coordinator.mutate(gameData, () ->
                reveals.revealToPlayer(
                        gameData,
                        ownerId,
                        GameEventFact.RevealZone.LIBRARY,
                        List.of(top),
                        controllerId));

        assertThat(messagesFor(controllerId))
                .singleElement()
                .isInstanceOfSatisfying(
                        RevealLibraryTopMessage.class,
                        message -> assertThat(message.cards())
                                .extracting(card -> card.name())
                                .containsExactly("Secret top card"));
        assertThat(messagesFor(ownerId)).isEmpty();
        assertThat(messagesFor(aiOpponentId)).isEmpty();

        sessions.deliveries.clear();
        coordinator.mutate(gameData, () ->
                reveals.revealToPlayer(
                        gameData,
                        ownerId,
                        GameEventFact.RevealZone.LIBRARY,
                        List.of(),
                        controllerId));

        assertThat(messagesFor(controllerId))
                .singleElement()
                .isInstanceOfSatisfying(
                        RevealLibraryTopMessage.class,
                        message -> assertThat(message.cards()).isEmpty());
    }

    @Test
    void revealProjectionRetainsIdentityWhileTheRevealedCardIsParkedForTargetSelection() {
        Card hidden = card("Parked revealed card");
        gameData.playerHands.get(ownerId).add(hidden);

        coordinator.mutate(gameData, () -> {
            reveals.revealToPlayer(
                    gameData,
                    ownerId,
                    GameEventFact.RevealZone.HAND,
                    List.of(hidden),
                    controllerId);
            gameData.playerHands.get(ownerId).remove(hidden);
            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.HandCastSpellTarget(
                            hidden, controllerId, List.of(), StackEntryType.SORCERY_SPELL));
        });

        assertThat(messagesFor(controllerId))
                .singleElement()
                .isInstanceOfSatisfying(
                        RevealHandMessage.class,
                        message -> assertThat(message.cards())
                                .extracting(card -> card.name())
                                .containsExactly("Parked revealed card"));
        assertThat(messagesFor(ownerId)).isEmpty();
        assertThat(messagesFor(aiOpponentId)).isEmpty();
    }

    private void assertPrivateRevealBatch(
            UUID subjectPlayerId, UUID recipientId, int cardCount) {
        assertThat(batches).singleElement().satisfies(batch -> {
            assertThat(batch.events())
                    .extracting(event -> event.kind())
                    .containsExactly(GameEventKind.PRIVATE_REVEAL);
            assertThat(batch.events().getFirst().audience().playerIds())
                    .containsExactly(recipientId);
            assertThat(batch.events().getFirst().fact())
                    .isInstanceOfSatisfying(
                            GameEventFact.PrivateReveal.class,
                            reveal -> {
                                assertThat(reveal.subjectPlayerId()).isEqualTo(subjectPlayerId);
                                assertThat(reveal.cards()).hasSize(cardCount);
                            });
        });
    }

    private List<Object> messagesFor(UUID playerId) {
        return sessions.deliveries.stream()
                .filter(delivery -> delivery.playerId().equals(playerId))
                .map(Delivery::message)
                .toList();
    }

    private void addPlayer(UUID playerId, String name) {
        gameData.playerIds.add(playerId);
        gameData.orderedPlayerIds.add(playerId);
        gameData.playerNames.add(name);
        gameData.playerIdToName.put(playerId, name);
        gameData.playerHands.put(playerId, new ArrayList<>());
        gameData.playerDecks.put(playerId, new ArrayList<>());
        gameData.playerGraveyards.put(playerId, new ArrayList<>());
        gameData.playerBattlefields.put(playerId, new ArrayList<>());
    }

    private static Card card(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }

    private record Delivery(UUID playerId, Object message) {
    }

    private static final class RecordingSessionManager implements SessionManager {

        private final List<Delivery> deliveries = new ArrayList<>();

        @Override
        public void registerPlayer(Connection connection, UUID userId, String username) {
        }

        @Override
        public void unregisterSession(String connectionId) {
        }

        @Override
        public Player getPlayer(String connectionId) {
            return null;
        }

        @Override
        public Connection getConnectionByUserId(UUID userId) {
            return null;
        }

        @Override
        public void setInGame(String connectionId) {
        }

        @Override
        public boolean isInGame(String connectionId) {
            return false;
        }

        @Override
        public void clearInGame(String connectionId) {
        }

        @Override
        public Collection<Player> getLobbyPlayers() {
            return List.of();
        }

        @Override
        public void sendToPlayer(UUID playerId, Object message) {
            deliveries.add(new Delivery(playerId, message));
        }

        @Override
        public void sendToPlayers(Collection<UUID> playerIds, Object message) {
            playerIds.forEach(playerId -> sendToPlayer(playerId, message));
        }
    }
}
