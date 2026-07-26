package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.event.GameEventAudience;
import com.github.laxika.magicalvibes.model.event.GameEventBatch;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.model.event.GameEventKind;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.event.GameEventDispatcher;
import com.github.laxika.magicalvibes.service.event.GameEventSubscriber;
import com.github.laxika.magicalvibes.service.event.GameMutationCoordinator;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.interaction.MayAbilityChoiceInteractionHandler;
import com.github.laxika.magicalvibes.service.state.StateBasedActionService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class InputCompletionServiceTest {

    @Mock private PlayerInputService playerInputService;
    @Mock private TurnProgressionService turnProgressionService;
    @Mock private StateBasedActionService stateBasedActionService;
    @Mock private EffectResolutionService effectResolutionService;

    private InputCompletionService service;
    private GameLogService gameLogService;
    private GameMutationCoordinator coordinator;
    private RecordingSubscriber events;
    private GameData gameData;
    private UUID playerId;

    @BeforeEach
    void setUp() {
        playerId = UUID.randomUUID();
        gameData = new GameData(UUID.randomUUID(), "test-game", playerId, "Alice");
        gameData.status = GameStatus.RUNNING;

        events = new RecordingSubscriber();
        coordinator = new GameMutationCoordinator(new GameEventDispatcher(List.of(events)));
        gameLogService = new GameLogService(coordinator);
        service = new InputCompletionService(
                playerInputService,
                coordinator,
                turnProgressionService,
                stateBasedActionService,
                effectResolutionService);
    }

    @Test
    void noFurtherInputPublishesOneStateObservationAfterAutoPass() {
        gameData.priorityPassedBy.add(playerId);
        doAnswer(invocation -> {
            assertThat(gameData.priorityPassedBy).isEmpty();
            return null;
        }).when(turnProgressionService).resolveAutoPass(gameData);

        mutate(() -> service.processMayAbilitiesThenAutoPass(gameData));

        assertThat(lastKinds()).containsExactly(GameEventKind.STATE_INVALIDATED);
        InOrder order = inOrder(playerInputService, turnProgressionService);
        order.verify(playerInputService).processNextMayAbility(gameData);
        order.verify(turnProgressionService).resolveAutoPass(gameData);
    }

    @Test
    void inputLogJoinsTheOuterAnswerBatchBeforeTheStableStateObservation() {
        doAnswer(invocation -> {
            assertThat(gameData.gameLog)
                    .extracting(entry -> entry.plainText())
                    .containsExactly("Alice chooses red.");
            return null;
        }).when(turnProgressionService).resolveAutoPass(gameData);

        mutate(() -> {
            gameLogService.append(gameData, GameLog.text("Alice chooses red."));
            service.processMayAbilitiesThenAutoPass(gameData);
        });

        assertThat(events.batches).singleElement().satisfies(batch -> {
            assertThat(batch.events())
                    .extracting(event -> event.fact().kind())
                    .containsExactly(
                            GameEventKind.GAME_LOG_APPENDED,
                            GameEventKind.STATE_INVALIDATED);
            assertThat(batch.events().getFirst().fact())
                    .isEqualTo(new GameEventFact.GameLogAppended(0));
            assertThat(batch.events())
                    .allSatisfy(event -> assertThat(event.audience())
                            .isEqualTo(GameEventAudience.allPlayers()));
        });
    }

    @Test
    void preservingPriorityVariantPublishesAfterAutoPassWithoutClearingPasses() {
        gameData.priorityPassedBy.add(playerId);
        doAnswer(invocation -> {
            assertThat(gameData.priorityPassedBy).containsExactly(playerId);
            return null;
        }).when(turnProgressionService).resolveAutoPass(gameData);

        mutate(() -> service.processMayAbilitiesThenAutoPassPreservingPriority(gameData));

        assertThat(lastKinds()).containsExactly(GameEventKind.STATE_INVALIDATED);
        assertThat(gameData.priorityPassedBy).containsExactly(playerId);
    }

    @Test
    void pendingMayAbilityPublishesItsDecisionAndDoesNotPublishStateOrAutoPass() {
        gameData.pendingMayAbilities.add(pendingMayAbility());
        InteractionHandlerRegistry interactions = mayInteractionRegistry();
        doAnswer(invocation -> {
            PendingMayAbility pending = gameData.pendingMayAbilities.getFirst();
            interactions.begin(gameData, new PendingInteraction.MayAbilityChoice(
                    pending.controllerId(), pending.description(), pending.manaCost()));
            return null;
        }).when(playerInputService).processNextMayAbility(gameData);

        mutate(() -> service.processMayAbilitiesThenAutoPass(gameData));

        assertThat(lastKinds()).containsExactly(GameEventKind.DECISION_REQUESTED);
        assertThat(gameData.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        verifyNoInteractions(turnProgressionService, effectResolutionService);
    }

    @Test
    void parkedResolutionResumesBeforeAutoPassAndPublishesOnlyAfterItIsDrained() {
        StackEntry parked = parkResolution();
        doAnswer(invocation -> {
            gameData.pendingEffectResolutionEntry = null;
            gameData.pendingEffectResolutionIndex = 0;
            return null;
        }).when(effectResolutionService).resolveEffectsFrom(gameData, parked, 2);
        doAnswer(invocation -> {
            assertThat(gameData.pendingEffectResolutionEntry).isNull();
            return null;
        }).when(turnProgressionService).resolveAutoPass(gameData);

        mutate(() -> service.processMayAbilitiesThenAutoPass(gameData));

        assertThat(lastKinds()).containsExactly(GameEventKind.STATE_INVALIDATED);
        InOrder order = inOrder(effectResolutionService, turnProgressionService);
        order.verify(effectResolutionService).resolveEffectsFrom(gameData, parked, 2);
        order.verify(turnProgressionService).resolveAutoPass(gameData);
    }

    @Test
    void interactionOpenedByParkedResolutionRemainsTheOnlyObservation() {
        StackEntry parked = parkResolution();
        InteractionHandlerRegistry interactions = mayInteractionRegistry();
        doAnswer(invocation -> {
            gameData.pendingEffectResolutionEntry = null;
            interactions.begin(gameData, new PendingInteraction.MayAbilityChoice(
                    playerId, "Choose the next mode", null));
            return null;
        }).when(effectResolutionService).resolveEffectsFrom(gameData, parked, 2);

        mutate(() -> service.processMayAbilitiesThenAutoPass(gameData));

        assertThat(lastKinds()).containsExactly(GameEventKind.DECISION_REQUESTED);
        verify(turnProgressionService, never()).resolveAutoPass(gameData);
    }

    @Test
    void chainedInputKeepsDecisionAsBarrierBeforePostAnswerState() {
        InteractionHandlerRegistry interactions = mayInteractionRegistry();

        mutate(() -> {
            interactions.begin(gameData, new PendingInteraction.MayAbilityChoice(
                    playerId, "Choose again", null));
            service.publishStateAfterInput(gameData);
        });

        assertThat(lastKinds()).containsExactly(
                GameEventKind.DECISION_REQUESTED,
                GameEventKind.STATE_INVALIDATED);
    }

    @Test
    void queuedInteractionDoesNotCoalesceAwayThePrePromptStateObservation() {
        InteractionHandlerRegistry interactions = mayInteractionRegistry();

        mutate(() -> {
            service.publishStateAfterInput(gameData);
            interactions.begin(gameData, new PendingInteraction.MayAbilityChoice(
                    playerId, "Choose again", null));
        });

        assertThat(lastKinds()).containsExactly(
                GameEventKind.STATE_INVALIDATED,
                GameEventKind.DECISION_REQUESTED);
    }

    @Test
    void autoPassAndCompletionInvalidationsCoalesceWithinOneAnswer() {
        doAnswer(invocation -> {
            coordinator.invalidateAllPlayerViews(gameData);
            return null;
        }).when(turnProgressionService).resolveAutoPass(gameData);

        mutate(() -> service.processMayAbilitiesThenAutoPass(gameData));

        assertThat(lastKinds()).containsExactly(GameEventKind.STATE_INVALIDATED);
    }

    @Test
    void gameEndDuringAutoPassPublishesGameEndWithoutAStateInvalidation() {
        doAnswer(invocation -> {
            gameData.status = GameStatus.FINISHED;
            coordinator.emit(
                    gameData,
                    new GameEventFact.GameEnded(GameEventFact.GameResult.DRAW, null),
                    GameEventAudience.allPlayers());
            return null;
        }).when(turnProgressionService).resolveAutoPass(gameData);

        mutate(() -> service.processMayAbilitiesThenAutoPass(gameData));

        assertThat(lastKinds()).containsExactly(GameEventKind.GAME_ENDED);
    }

    @Test
    void stateBasedActionsRunBeforeTheStableCompletionObservation() {
        doAnswer(invocation -> {
            gameData.playerLifeTotals.put(playerId, 7);
            return null;
        }).when(stateBasedActionService).performStateBasedActions(gameData);
        doAnswer(invocation -> {
            assertThat(gameData.playerLifeTotals.get(playerId)).isEqualTo(7);
            return null;
        }).when(turnProgressionService).resolveAutoPass(gameData);

        mutate(() -> service.sbaProcessMayAbilitiesThenAutoPass(gameData));

        assertThat(lastKinds()).containsExactly(GameEventKind.STATE_INVALIDATED);
        InOrder order = inOrder(stateBasedActionService, turnProgressionService);
        order.verify(stateBasedActionService).performStateBasedActions(gameData);
        order.verify(turnProgressionService).resolveAutoPass(gameData);
    }

    @Test
    void interactionOpenedByStateBasedActionsDefersMayAbilitiesAndParkedResolution() {
        StackEntry parked = parkResolution();
        gameData.pendingMayAbilities.add(pendingMayAbility());
        InteractionHandlerRegistry interactions = mayInteractionRegistry();
        doAnswer(invocation -> {
            interactions.begin(gameData, new PendingInteraction.MayAbilityChoice(
                    playerId, "Choose a legendary permanent to keep", null));
            return null;
        }).when(stateBasedActionService).performStateBasedActions(gameData);

        mutate(() -> service.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData));

        assertThat(lastKinds()).containsExactly(GameEventKind.DECISION_REQUESTED);
        assertThat(gameData.pendingEffectResolutionEntry).isSameAs(parked);
        verify(playerInputService, never()).processNextMayAbility(gameData);
        verify(effectResolutionService, never()).resolveEffectsFrom(gameData, parked, 2);
        verify(turnProgressionService, never()).resolveAutoPass(gameData);
    }

    @Test
    void manaAbilityCompletionPublishesAfterSbaAndAutoPassWithoutResumingAnUnrelatedPark() {
        StackEntry parked = parkResolution();
        doAnswer(invocation -> {
            assertThat(gameData.pendingEffectResolutionEntry).isSameAs(parked);
            return null;
        }).when(turnProgressionService).resolveAutoPass(gameData);

        mutate(() -> service.sbaThenAutoPassWithoutResumingParkedResolution(gameData));

        assertThat(lastKinds()).containsExactly(GameEventKind.STATE_INVALIDATED);
        assertThat(gameData.pendingEffectResolutionEntry).isSameAs(parked);
        InOrder order = inOrder(stateBasedActionService, turnProgressionService);
        order.verify(stateBasedActionService).performStateBasedActions(gameData);
        order.verify(turnProgressionService).resolveAutoPass(gameData);
        verifyNoInteractions(effectResolutionService);
    }

    @Test
    void validationExceptionDiscardsAnIncompleteAnswersObservation() {
        assertThatThrownBy(() -> mutate(() -> {
            service.publishStateAfterInput(gameData);
            throw new IllegalStateException("invalid answer");
        }))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("invalid answer");

        assertThat(events.batches).isEmpty();
    }

    @Test
    void alreadyFinishedGameDoesNothing() {
        gameData.status = GameStatus.FINISHED;

        service.processMayAbilitiesThenAutoPass(gameData);

        verifyNoInteractions(
                playerInputService,
                turnProgressionService,
                stateBasedActionService,
                effectResolutionService);
        assertThat(events.batches).isEmpty();
    }

    private InteractionHandlerRegistry mayInteractionRegistry() {
        InteractionHandlerRegistry registry = new InteractionHandlerRegistry(() -> coordinator);
        registry.register(new MayAbilityChoiceInteractionHandler(mock(MayAbilityHandlerService.class)));
        return registry;
    }

    private StackEntry parkResolution() {
        Card card = namedCard("Paused ability");
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY, card, playerId, card.getName(), List.of());
        gameData.pendingEffectResolutionEntry = entry;
        gameData.pendingEffectResolutionIndex = 2;
        return entry;
    }

    private PendingMayAbility pendingMayAbility() {
        Card card = namedCard("Optional ability");
        return new PendingMayAbility(card, playerId, List.of(), "Use optional ability?");
    }

    private static Card namedCard(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }

    private void mutate(Runnable operation) {
        coordinator.mutate(gameData, operation);
    }

    private List<GameEventKind> lastKinds() {
        return events.batches.getLast().events().stream()
                .map(event -> event.fact().kind())
                .toList();
    }

    private static final class RecordingSubscriber implements GameEventSubscriber {
        private final List<GameEventBatch> batches = new ArrayList<>();

        @Override
        public void onGameEvents(GameEventBatch batch) {
            batches.add(batch);
        }
    }
}
