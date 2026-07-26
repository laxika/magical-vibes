package com.github.laxika.magicalvibes.service.event;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.CombatAttackTarget;
import com.github.laxika.magicalvibes.model.CombatDamageTarget;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.event.GameEventAudience;
import com.github.laxika.magicalvibes.model.event.GameEventBatch;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.networking.Connection;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.GameOverMessage;
import com.github.laxika.magicalvibes.networking.message.GameStateMessage;
import com.github.laxika.magicalvibes.networking.message.InteractionPromptMessage;
import com.github.laxika.magicalvibes.networking.message.AvailableAttackersMessage;
import com.github.laxika.magicalvibes.networking.message.AvailableBlockersMessage;
import com.github.laxika.magicalvibes.networking.message.CombatDamageAssignmentNotification;
import com.github.laxika.magicalvibes.networking.message.MulliganResolvedMessage;
import com.github.laxika.magicalvibes.networking.message.RevealHandMessage;
import com.github.laxika.magicalvibes.networking.message.SelectCardsToBottomMessage;
import com.github.laxika.magicalvibes.networking.model.GameLogEntryView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.networking.service.GameLogViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameMessageTransport;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.GameViewProjectionFactory;
import com.github.laxika.magicalvibes.service.PrivateInformationProjectionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GameEventProjectionSubscriberTest {

    private UUID player1Id;
    private UUID player2Id;
    private GameData gameData;
    private GameRegistry gameRegistry;
    private RecordingSessionManager sessions;
    private GameViewProjectionFactory gameViewProjectionFactory;
    private PrivateInformationProjectionFactory privateInformationProjectionFactory;
    private GameLogViewFactory gameLogViewFactory;
    private InteractionPromptProjectionRegistry interactionPromptProjectionRegistry;
    private GameMessageTransport transport;
    private GameEventProjectionSubscriber subscriber;

    @BeforeEach
    void setUp() {
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gameData = new GameData(UUID.randomUUID(), "projection-test", player1Id, "Player 1");
        gameData.orderedPlayerIds.addAll(List.of(player1Id, player2Id));
        gameData.playerIds.addAll(List.of(player1Id, player2Id));
        gameData.playerNames.addAll(List.of("Player 1", "Player 2"));
        gameData.playerIdToName.put(player1Id, "Player 1");
        gameData.playerIdToName.put(player2Id, "Player 2");
        gameData.status = GameStatus.RUNNING;
        gameData.activePlayerId = player1Id;
        gameData.currentStep = TurnStep.PRECOMBAT_MAIN;

        gameRegistry = new GameRegistry();
        gameRegistry.register(gameData);
        sessions = new RecordingSessionManager();
        transport = new GameMessageTransport(sessions);
        gameViewProjectionFactory = mock(GameViewProjectionFactory.class);
        privateInformationProjectionFactory = mock(PrivateInformationProjectionFactory.class);
        gameLogViewFactory = mock(GameLogViewFactory.class);
        interactionPromptProjectionRegistry =
                new InteractionPromptProjectionRegistry(mock(CardViewFactory.class));
        subscriber = new GameEventProjectionSubscriber(
                gameRegistry,
                gameViewProjectionFactory,
                privateInformationProjectionFactory,
                interactionPromptProjectionRegistry,
                gameLogViewFactory,
                transport);
    }

    @Test
    void stateEventProducesTheSamePlayerSpecificMessagesAsTheLegacyFacadeAfterMutationAndUnlock() {
        GameStateMessage player1Message = stateMessage(player1Id, List.of(0));
        GameStateMessage player2Message = stateMessage(player2Id, List.of(1));
        Map<UUID, GameStateMessage> projected = new LinkedHashMap<>();
        projected.put(player1Id, player1Message);
        projected.put(player2Id, player2Message);

        when(gameViewProjectionFactory.createGameStateMessages(any(), any())).thenAnswer(invocation -> {
            assertThat(Thread.holdsLock(gameData)).isFalse();
            assertThat(gameData.turnNumber).isEqualTo(17);
            return projected;
        });

        GameBroadcastService legacy = new GameBroadcastService(
                gameViewProjectionFactory,
                privateInformationProjectionFactory,
                transport,
                gameLogViewFactory,
                mock(com.github.laxika.magicalvibes.service.battlefield.GameQueryService.class));
        gameData.turnNumber = 17;
        legacy.broadcastGameState(gameData);
        List<Delivery> legacyDeliveries = List.copyOf(sessions.deliveries);

        sessions.deliveries.clear();
        GameMutationCoordinator coordinator = new GameMutationCoordinator(
                new GameEventDispatcher(List.of(subscriber)));
        coordinator.mutate(gameData, UUID.randomUUID(), () -> {
            gameData.turnNumber = 17;
            coordinator.emit(gameData,
                    new GameEventFact.StateInvalidated(
                            GameEventFact.StateSection.PRIVATE_PLAYER_VIEW),
                    GameEventAudience.allPlayers());
        });

        assertThat(sessions.deliveries).containsExactlyElementsOf(legacyDeliveries);
        assertThat(sessions.deliveries).containsExactly(
                new Delivery(player1Id, player1Message),
                new Delivery(player2Id, player2Message));
    }

    @Test
    void privateRevealReachesOnlyItsExplicitAudience() {
        RevealHandMessage revealMessage = new RevealHandMessage(List.of(), "Player 2");
        when(privateInformationProjectionFactory.createReveal(any(), any()))
                .thenReturn(revealMessage);

        GameMutationCoordinator coordinator = coordinator();
        coordinator.mutate(gameData, UUID.randomUUID(), () ->
                coordinator.emit(gameData, new GameEventFact.PrivateReveal(
                                UUID.randomUUID(),
                                player2Id,
                                GameEventFact.RevealZone.HAND,
                                List.of(new GameEventFact.CardSnapshot(
                                        UUID.randomUUID(), "Private card", "TST", "1"))),
                        GameEventAudience.player(player1Id)));

        assertThat(sessions.deliveries).containsExactly(new Delivery(player1Id, revealMessage));
    }

    @Test
    void standardInteractionDecisionProjectsTheExistingPromptMessage() {
        gameData.interaction.beginInteraction(new PendingInteraction.XValueChoice(
                player1Id, 7, "Choose X.", "Test Card"));

        emitActiveDecision(
                player1Id,
                GameEventFact.DecisionKind.INTERACTION,
                GameEventAudience.player(player1Id));

        assertThat(messagesFor(player1Id))
                .singleElement()
                .isInstanceOfSatisfying(InteractionPromptMessage.class, prompt -> {
                    assertThat(prompt.prompt()).isEqualTo("Choose X.");
                    assertThat(prompt.maxCount()).isEqualTo(7);
                    assertThat(prompt.cardName()).isEqualTo("Test Card");
                });
        assertThat(messagesFor(player2Id)).isEmpty();
    }

    @Test
    void staleInteractionIdentityCannotProjectAnAlreadyAnsweredPrompt() {
        gameData.interaction.beginInteraction(new PendingInteraction.XValueChoice(
                player1Id, 7, "Choose X.", "Test Card"));

        GameMutationCoordinator coordinator = coordinator();
        coordinator.mutate(gameData, UUID.randomUUID(), () ->
                coordinator.emit(gameData,
                        new GameEventFact.DecisionRequested(
                                UUID.randomUUID(),
                                player1Id,
                                GameEventFact.DecisionKind.INTERACTION),
                        GameEventAudience.player(player1Id)));

        assertThat(sessions.deliveries).isEmpty();
    }

    @Test
    void consecutiveInteractionsRemainDistinctFactsAndOnlyTheCurrentIdentityProjects() {
        List<GameEventBatch> recorded = new ArrayList<>();
        GameMutationCoordinator coordinator = new GameMutationCoordinator(
                new GameEventDispatcher(List.of(subscriber, recorded::add)));

        coordinator.mutate(gameData, UUID.randomUUID(), () -> {
            gameData.interaction.beginInteraction(new PendingInteraction.XValueChoice(
                    player1Id, 3, "First prompt.", "First"));
            coordinator.emit(gameData,
                    new GameEventFact.DecisionRequested(
                            gameData.interaction.activeDecisionId(),
                            player1Id,
                            GameEventFact.DecisionKind.INTERACTION),
                    GameEventAudience.player(player1Id));

            gameData.interaction.beginInteraction(new PendingInteraction.ColorChoice(
                    player1Id, null, null, null, List.of("WHITE", "BLUE"), "Second prompt."));
            coordinator.emit(gameData,
                    new GameEventFact.DecisionRequested(
                            gameData.interaction.activeDecisionId(),
                            player1Id,
                            GameEventFact.DecisionKind.INTERACTION),
                    GameEventAudience.player(player1Id));
        });

        assertThat(recorded).singleElement().satisfies(batch ->
                assertThat(batch.events())
                        .hasSize(2)
                        .allSatisfy(event -> assertThat(event.fact())
                                .isInstanceOf(GameEventFact.DecisionRequested.class)));
        assertThat(messagesFor(player1Id))
                .singleElement()
                .isInstanceOfSatisfying(InteractionPromptMessage.class,
                        prompt -> assertThat(prompt.prompt()).isEqualTo("Second prompt."));
    }

    @Test
    void attackerDecisionProjectsFinalizedTargetsRequirementsAndTaxOnlyToItsAudience() {
        UUID planeswalkerId = UUID.randomUUID();
        gameData.interaction.beginInteraction(new PendingInteraction.AttackerDeclaration(
                player2Id,
                List.of(1, 3),
                List.of(3),
                List.of(
                        new CombatAttackTarget(player1Id, "Player 1", true),
                        new CombatAttackTarget(planeswalkerId, "Planeswalker", false)),
                2,
                true));

        emitActiveDecision(
                player2Id,
                GameEventFact.DecisionKind.ATTACKER_DECLARATION,
                GameEventAudience.players(player1Id, player2Id));

        assertThat(sessions.deliveries)
                .singleElement()
                .satisfies(delivery -> {
                    assertThat(delivery.playerId()).isEqualTo(player2Id);
                    assertThat(delivery.message()).isInstanceOfSatisfying(
                            AvailableAttackersMessage.class, message -> {
                                assertThat(message.attackerIndices()).containsExactly(1, 3);
                                assertThat(message.mustAttackIndices()).containsExactly(3);
                                assertThat(message.availableTargets())
                                        .extracting(target -> target.id())
                                        .containsExactly(player1Id.toString(), planeswalkerId.toString());
                                assertThat(message.taxPerCreature()).isEqualTo(2);
                                assertThat(message.mustAttackWithAtLeastOne()).isTrue();
                            });
                });
    }

    @Test
    void blockerDecisionProjectsFinalizedLegalPairsAndRequirements() {
        gameData.interaction.beginInteraction(new PendingInteraction.BlockerDeclaration(
                player2Id,
                List.of(0, 2),
                List.of(1, 3),
                Map.of(0, List.of(1), 2, List.of(1, 3)),
                List.of(3),
                List.of(1),
                Map.of(2, List.of(3))));

        emitActiveDecision(
                player2Id,
                GameEventFact.DecisionKind.BLOCKER_DECLARATION,
                GameEventAudience.player(player2Id));

        assertThat(messagesFor(player2Id))
                .singleElement()
                .isInstanceOfSatisfying(AvailableBlockersMessage.class, message -> {
                    assertThat(message.blockerIndices()).containsExactly(0, 2);
                    assertThat(message.attackerIndices()).containsExactly(1, 3);
                    assertThat(message.legalBlockPairs())
                            .containsEntry(0, List.of(1))
                            .containsEntry(2, List.of(1, 3));
                    assertThat(message.mustBeBlockedAttackerIndices()).containsExactly(3);
                    assertThat(message.menaceAttackerIndices()).containsExactly(1);
                    assertThat(message.mustBlockRequirements()).containsEntry(2, List.of(3));
                });
    }

    @Test
    void combatDamageDecisionProjectsTrampleDeathtouchAndAssignmentTargets() {
        UUID attackerId = UUID.randomUUID();
        UUID blockerId = UUID.randomUUID();
        gameData.interaction.beginInteraction(new PendingInteraction.CombatDamageAssignment(
                player1Id,
                4,
                attackerId,
                "Attacker",
                6,
                List.of(
                        new CombatDamageTarget(blockerId, "Blocker", 5, 1, false),
                        new CombatDamageTarget(player2Id, "Player 2", 0, 0, true)),
                true,
                true,
                false));

        emitActiveDecision(
                player1Id,
                GameEventFact.DecisionKind.COMBAT_DAMAGE_ASSIGNMENT,
                GameEventAudience.player(player1Id));

        assertThat(messagesFor(player1Id))
                .singleElement()
                .isInstanceOfSatisfying(CombatDamageAssignmentNotification.class, message -> {
                    assertThat(message.attackerIndex()).isEqualTo(4);
                    assertThat(message.attackerPermanentId()).isEqualTo(attackerId.toString());
                    assertThat(message.totalDamage()).isEqualTo(6);
                    assertThat(message.isTrample()).isTrue();
                    assertThat(message.isDeathtouch()).isTrue();
                    assertThat(message.validTargets())
                            .extracting(target -> target.id())
                            .containsExactly(blockerId.toString(), player2Id.toString());
                });
    }

    @Test
    void gameEndMulliganBottomCardsAndInteractionEventsUseExistingWireMessages() {
        gameData.interaction.beginInteraction(
                new PendingInteraction.AttackerDeclaration(player2Id));
        gameData.playerNeedsToBottom.put(player1Id, 2);

        GameMutationCoordinator coordinator = coordinator();
        coordinator.mutate(gameData, UUID.randomUUID(), () -> {
            coordinator.emit(gameData,
                    new GameEventFact.MulliganResolved(player1Id, false, 1),
                    GameEventAudience.allPlayers());
            coordinator.emit(gameData,
                    new GameEventFact.DecisionRequested(
                            UUID.randomUUID(),
                            player1Id,
                            GameEventFact.DecisionKind.CARDS_TO_BOTTOM),
                    GameEventAudience.player(player1Id));
            coordinator.emit(gameData,
                    new GameEventFact.DecisionRequested(
                            gameData.interaction.activeDecisionId(),
                            player2Id,
                            GameEventFact.DecisionKind.ATTACKER_DECLARATION,
                            GameEventFact.DecisionDelivery.REPLAY_REQUESTED),
                    GameEventAudience.player(player2Id));
            coordinator.emit(gameData,
                    new GameEventFact.GameEnded(GameEventFact.GameResult.WIN, player1Id),
                    GameEventAudience.allPlayers());
        });

        assertThat(messagesFor(player1Id))
                .anySatisfy(message -> assertThat(message).isInstanceOf(MulliganResolvedMessage.class))
                .anySatisfy(message -> assertThat(message).isEqualTo(new SelectCardsToBottomMessage(2)))
                .anySatisfy(message -> assertThat(message).isEqualTo(
                        new GameOverMessage(player1Id, "Player 1")));
        assertThat(messagesFor(player2Id))
                .anySatisfy(message -> assertThat(message).isInstanceOf(MulliganResolvedMessage.class))
                .anySatisfy(message -> assertThat(message).isInstanceOf(AvailableAttackersMessage.class))
                .anySatisfy(message -> assertThat(message).isInstanceOf(GameOverMessage.class));
    }

    @Test
    void mindslaverDecisionIsProjectedOnlyToTheAuthoritativeDecisionOwner() {
        gameData.mindControlledPlayerId = player2Id;
        gameData.mindControllerPlayerId = player1Id;
        gameData.interaction.beginInteraction(
                new PendingInteraction.AttackerDeclaration(player2Id));

        GameMutationCoordinator coordinator = coordinator();
        coordinator.mutate(gameData, UUID.randomUUID(), () ->
                coordinator.emit(gameData,
                        new GameEventFact.DecisionRequested(
                                gameData.interaction.activeDecisionId(),
                                player2Id,
                                GameEventFact.DecisionKind.ATTACKER_DECLARATION),
                        GameEventAudience.players(player1Id, player2Id)));

        assertThat(sessions.deliveries)
                .singleElement()
                .satisfies(delivery -> {
                    assertThat(delivery.playerId()).isEqualTo(player1Id);
                    assertThat(delivery.message()).isInstanceOf(AvailableAttackersMessage.class);
                });
    }

    @Test
    void decisionBarrierPreservesBothStateMessagesWithoutDuplicatingLogEntries() {
        gameData.interaction.beginInteraction(
                new PendingInteraction.AttackerDeclaration(player2Id));
        gameData.gameLog.add(GameLog.text("One new log entry."));
        GameLogEntryView logEntry = mock(GameLogEntryView.class);
        when(gameLogViewFactory.createAll(any())).thenReturn(List.of(logEntry));

        GameStateMessage withLogs = stateMessage(player2Id, List.of(1));
        GameStateMessage withoutLogs = stateMessage(player2Id, List.of(2));
        when(gameViewProjectionFactory.createGameStateMessages(
                any(), argThat(entries -> entries != null && !entries.isEmpty())))
                .thenReturn(Map.of(player1Id, withLogs, player2Id, withLogs));
        when(gameViewProjectionFactory.createGameStateMessages(
                any(), argThat(entries -> entries != null && entries.isEmpty())))
                .thenReturn(Map.of(player1Id, withoutLogs, player2Id, withoutLogs));

        GameMutationCoordinator coordinator = coordinator();
        coordinator.mutate(gameData, UUID.randomUUID(), () -> {
            coordinator.invalidateAllPlayerViews(gameData);
            coordinator.emit(gameData,
                    new GameEventFact.DecisionRequested(
                            gameData.interaction.activeDecisionId(),
                            player2Id,
                            GameEventFact.DecisionKind.ATTACKER_DECLARATION),
                    GameEventAudience.player(player2Id));
            coordinator.invalidateAllPlayerViews(gameData);
        });

        assertThat(messagesFor(player2Id)).containsExactly(
                withLogs,
                sessions.deliveries.stream()
                        .filter(delivery -> delivery.playerId().equals(player2Id))
                        .map(Delivery::message)
                        .filter(AvailableAttackersMessage.class::isInstance)
                        .findFirst()
                        .orElseThrow(),
                withoutLogs);
        verify(gameViewProjectionFactory).createGameStateMessages(
                gameData, List.of(logEntry));
        verify(gameViewProjectionFactory).createGameStateMessages(
                gameData, List.of());
    }

    @Test
    void transportFailureForOnePlayerIsLoggedAndDoesNotBlockAnother() {
        GameStateMessage player1Message = stateMessage(player1Id, List.of(0));
        GameStateMessage player2Message = stateMessage(player2Id, List.of(1));
        when(gameViewProjectionFactory.createGameStateMessages(any(), any()))
                .thenReturn(Map.of(player1Id, player1Message, player2Id, player2Message));
        sessions.failingPlayerId = player1Id;

        assertThatCode(() -> {
            GameMutationCoordinator coordinator = coordinator();
            coordinator.mutate(gameData, UUID.randomUUID(), () ->
                    coordinator.emit(gameData,
                            new GameEventFact.StateInvalidated(
                                    GameEventFact.StateSection.GAME_STATUS),
                            GameEventAudience.allPlayers()));
        }).doesNotThrowAnyException();

        assertThat(sessions.deliveries)
                .containsExactly(new Delivery(player2Id, player2Message));
    }

    @Test
    void simulationProducesNoOutboundMessages() {
        gameData.simulation = true;
        GameMutationCoordinator coordinator = coordinator();

        coordinator.mutate(gameData, UUID.randomUUID(), () -> {
            coordinator.emit(gameData,
                    new GameEventFact.MulliganResolved(player1Id, true, 0),
                    GameEventAudience.allPlayers());
            coordinator.emit(gameData,
                    new GameEventFact.GameEnded(GameEventFact.GameResult.DRAW, null),
                    GameEventAudience.allPlayers());
        });

        assertThat(sessions.deliveries).isEmpty();
    }

    private GameMutationCoordinator coordinator() {
        return new GameMutationCoordinator(new GameEventDispatcher(List.of(subscriber)));
    }

    private void emitActiveDecision(
            UUID decidingPlayerId,
            GameEventFact.DecisionKind decisionKind,
            GameEventAudience audience) {
        GameMutationCoordinator coordinator = coordinator();
        coordinator.mutate(gameData, UUID.randomUUID(), () ->
                coordinator.emit(gameData,
                        new GameEventFact.DecisionRequested(
                                gameData.interaction.activeDecisionId(),
                                decidingPlayerId,
                                decisionKind),
                        audience));
    }

    private List<Object> messagesFor(UUID playerId) {
        return sessions.deliveries.stream()
                .filter(delivery -> delivery.playerId().equals(playerId))
                .map(Delivery::message)
                .toList();
    }

    private static GameStateMessage stateMessage(UUID priorityPlayerId, List<Integer> playableIndices) {
        return new GameStateMessage(
                GameStatus.RUNNING,
                priorityPlayerId,
                17,
                TurnStep.PRECOMBAT_MAIN,
                priorityPlayerId,
                List.of(),
                List.of(),
                List.of(),
                List.of(40, 40),
                List.of(7, 7),
                List.of(20, 20),
                List.of(0, 0),
                List.of(),
                List.of(),
                0,
                Map.of(),
                List.of(TurnStep.PRECOMBAT_MAIN),
                playableIndices,
                List.of(),
                List.of(),
                List.of(),
                0,
                null,
                List.of(),
                List.of(),
                List.of(),
                playableIndices,
                0,
                Map.of());
    }

    private record Delivery(UUID playerId, Object message) {
    }

    private static final class RecordingSessionManager implements SessionManager {
        private final List<Delivery> deliveries = new ArrayList<>();
        private UUID failingPlayerId;

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
            if (playerId.equals(failingPlayerId)) {
                throw new IllegalStateException("simulated connection failure");
            }
            deliveries.add(new Delivery(playerId, message));
        }

        @Override
        public void sendToPlayers(Collection<UUID> playerIds, Object message) {
            playerIds.forEach(playerId -> sendToPlayer(playerId, message));
        }
    }
}
