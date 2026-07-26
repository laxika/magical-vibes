package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.AvailableAttackersMessage;
import com.github.laxika.magicalvibes.networking.message.AvailableBlockersMessage;
import com.github.laxika.magicalvibes.networking.message.CombatDamageAssignmentNotification;
import com.github.laxika.magicalvibes.networking.message.InteractionPromptMessage;
import com.github.laxika.magicalvibes.networking.message.SelectCardsToBottomMessage;
import com.github.laxika.magicalvibes.networking.model.InteractionShape;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.event.GameEventDispatcher;
import com.github.laxika.magicalvibes.service.event.GameMutationCoordinator;
import com.github.laxika.magicalvibes.service.event.InteractionPromptProjectionRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class ReconnectionServiceTest {

    private UUID player1Id;
    private UUID player2Id;
    private GameData gameData;
    private SessionManager sessions;
    private ReconnectionService reconnectionService;

    @BeforeEach
    void setUp() {
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gameData = new GameData(UUID.randomUUID(), "reconnect", player1Id, "Player 1");
        gameData.playerIds.add(player2Id);
        gameData.orderedPlayerIds.add(player2Id);
        gameData.playerIdToName.put(player2Id, "Player 2");

        sessions = mock(SessionManager.class);
        GameMutationCoordinator coordinator =
                new GameMutationCoordinator(new GameEventDispatcher(List.of()));
        InteractionPromptProjectionRegistry projections =
                new InteractionPromptProjectionRegistry(mock(CardViewFactory.class));
        reconnectionService = new ReconnectionService(
                coordinator, projections, new GameMessageTransport(sessions));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("projectionShapes")
    void reconnectProjectsEveryCanonicalInteractionShapeWithoutCreatingAnEvent(
            String name,
            Function<UUID, PendingInteraction> interactionFactory,
            Class<?> expectedMessageType,
            InteractionShape expectedShape) {
        gameData.interaction.beginInteraction(interactionFactory.apply(player1Id));
        long sequenceBefore = gameData.domainEventSequence();
        long versionBefore = gameData.domainStateVersion();

        reconnectionService.resendAwaitingInput(gameData, player1Id);

        ArgumentCaptor<Object> message = ArgumentCaptor.forClass(Object.class);
        verify(sessions).sendToPlayer(org.mockito.ArgumentMatchers.eq(player1Id), message.capture());
        assertThat(message.getValue()).isInstanceOf(expectedMessageType);
        if (expectedShape != null) {
            assertThat(message.getValue())
                    .isInstanceOfSatisfying(
                            InteractionPromptMessage.class,
                            prompt -> assertThat(prompt.shape()).isEqualTo(expectedShape));
        }
        assertThat(gameData.domainEventSequence()).isEqualTo(sequenceBefore);
        assertThat(gameData.domainStateVersion()).isEqualTo(versionBefore);
    }

    private static Stream<Arguments> projectionShapes() {
        return Stream.of(
                Arguments.of(
                        "card index",
                        (Function<UUID, PendingInteraction>) id ->
                                new PendingInteraction.HandCardChoice(id, List.of(0), "Choose."),
                        InteractionPromptMessage.class,
                        InteractionShape.CARD_INDEX_PICK),
                Arguments.of(
                        "graveyard index",
                        (Function<UUID, PendingInteraction>) id ->
                                PendingInteraction.GraveyardChoice
                                        .builder(
                                                id,
                                                List.of(0),
                                                com.github.laxika.magicalvibes.model
                                                        .GraveyardChoiceDestination.HAND,
                                                "Choose.")
                                        .build(),
                        InteractionPromptMessage.class,
                        InteractionShape.GRAVEYARD_INDEX_PICK),
                Arguments.of(
                        "library index",
                        (Function<UUID, PendingInteraction>) id ->
                                new PendingInteraction.LibrarySearch(
                                        LibrarySearchParams.builder(id, List.of()).build(),
                                        "Search.",
                                        true),
                        InteractionPromptMessage.class,
                        InteractionShape.LIBRARY_INDEX_PICK),
                Arguments.of(
                        "permanent",
                        (Function<UUID, PendingInteraction>) id ->
                                new PendingInteraction.PermanentChoice(
                                        id, List.of(), List.of(), null, "Choose."),
                        InteractionPromptMessage.class,
                        InteractionShape.PERMANENT_PICK),
                Arguments.of(
                        "multiple cards",
                        (Function<UUID, PendingInteraction>) id ->
                                new PendingInteraction.MirrorOfFateChoice(id, List.of(), 0),
                        InteractionPromptMessage.class,
                        InteractionShape.MULTI_CARD_PICK),
                Arguments.of(
                        "multiple permanents",
                        (Function<UUID, PendingInteraction>) id ->
                                new PendingInteraction.MultiPermanentChoice(
                                        id, List.of(), 0, null, "Choose."),
                        InteractionPromptMessage.class,
                        InteractionShape.MULTI_PERMANENT_PICK),
                Arguments.of(
                        "list",
                        (Function<UUID, PendingInteraction>) id ->
                                new PendingInteraction.ColorChoice(
                                        id, null, null, null, List.of("WHITE"), "Choose."),
                        InteractionPromptMessage.class,
                        InteractionShape.LIST_PICK),
                Arguments.of(
                        "accept decline",
                        (Function<UUID, PendingInteraction>) id ->
                                new PendingInteraction.AdNauseamRepeatChoice(id, "Source"),
                        InteractionPromptMessage.class,
                        InteractionShape.ACCEPT_DECLINE),
                Arguments.of(
                        "number",
                        (Function<UUID, PendingInteraction>) id ->
                                new PendingInteraction.XValueChoice(id, 3, "Choose.", "Source"),
                        InteractionPromptMessage.class,
                        InteractionShape.NUMBER_PICK),
                Arguments.of(
                        "scry order",
                        (Function<UUID, PendingInteraction>) id ->
                                new PendingInteraction.Scry(id, List.of()),
                        InteractionPromptMessage.class,
                        InteractionShape.SCRY_ORDER),
                Arguments.of(
                        "card order",
                        (Function<UUID, PendingInteraction>) id ->
                                new PendingInteraction.LibraryReorder(
                                        id, List.of(), false, null, "Order."),
                        InteractionPromptMessage.class,
                        InteractionShape.CARD_ORDER),
                Arguments.of(
                        "hand top bottom",
                        (Function<UUID, PendingInteraction>) id ->
                                new PendingInteraction.HandTopBottomChoice(id, List.of()),
                        InteractionPromptMessage.class,
                        InteractionShape.HAND_TOP_BOTTOM),
                Arguments.of(
                        "attackers",
                        (Function<UUID, PendingInteraction>)
                                PendingInteraction.AttackerDeclaration::new,
                        AvailableAttackersMessage.class,
                        null),
                Arguments.of(
                        "blockers",
                        (Function<UUID, PendingInteraction>)
                                PendingInteraction.BlockerDeclaration::new,
                        AvailableBlockersMessage.class,
                        null),
                Arguments.of(
                        "combat damage",
                        (Function<UUID, PendingInteraction>) id ->
                                new PendingInteraction.CombatDamageAssignment(
                                        id,
                                        0,
                                        UUID.randomUUID(),
                                        "Attacker",
                                        1,
                                        List.of(),
                                        false,
                                        false,
                                        false),
                        CombatDamageAssignmentNotification.class,
                        null));
    }

    @Test
    void reconnectingPlayerWhoDoesNotOwnTheCurrentDecisionReceivesNothing() {
        gameData.interaction.beginInteraction(
                new PendingInteraction.XValueChoice(player1Id, 3, "Choose.", "Source"));

        reconnectionService.resendAwaitingInput(gameData, player2Id);

        verifyNoInteractions(sessions);
    }

    @Test
    void mindControlledDecisionReplaysOnlyToTheController() {
        gameData.mindControlledPlayerId = player2Id;
        gameData.mindControllerPlayerId = player1Id;
        gameData.interaction.beginInteraction(
                new PendingInteraction.AttackerDeclaration(player2Id));

        reconnectionService.resendAwaitingInput(gameData, player2Id);
        verify(sessions, never()).sendToPlayer(
                org.mockito.ArgumentMatchers.eq(player2Id),
                org.mockito.ArgumentMatchers.any());

        reconnectionService.resendAwaitingInput(gameData, player1Id);
        verify(sessions).sendToPlayer(
                org.mockito.ArgumentMatchers.eq(player1Id),
                org.mockito.ArgumentMatchers.isA(AvailableAttackersMessage.class));
    }

    @Test
    void currentCardsToBottomDecisionReplaysWithoutMutationButStaleIdentityDoesNot() {
        UUID decisionId = UUID.randomUUID();
        gameData.playerBottomDecisionIds.put(player1Id, decisionId);
        long sequenceBefore = gameData.domainEventSequence();
        long versionBefore = gameData.domainStateVersion();

        reconnectionService.resendAwaitingInput(gameData, player1Id);
        verifyNoInteractions(sessions);

        gameData.playerNeedsToBottom.put(player1Id, 2);
        reconnectionService.resendAwaitingInput(gameData, player1Id);
        verify(sessions).sendToPlayer(player1Id, new SelectCardsToBottomMessage(2));
        assertThat(gameData.domainEventSequence()).isEqualTo(sequenceBefore);
        assertThat(gameData.domainStateVersion()).isEqualTo(versionBefore);
    }
}
