package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.state.StateBasedActionService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class InputCompletionServiceTest {

    @Mock private PlayerInputService playerInputService;
    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private TurnProgressionService turnProgressionService;
    @Mock private StateBasedActionService stateBasedActionService;
    @Mock private EffectResolutionService effectResolutionService;

    @InjectMocks
    private InputCompletionService service;

    private GameData gameData;
    private UUID playerId;

    @BeforeEach
    void setUp() {
        playerId = UUID.randomUUID();
        gameData = new GameData(UUID.randomUUID(), "test-game", playerId, "Alice");
        gameData.status = GameStatus.RUNNING;
    }

    @Nested
    class ProcessMayAbilitiesThenAutoPass {

        @Test
        void finishedGameDoesNothing() {
            gameData.status = GameStatus.FINISHED;

            service.processMayAbilitiesThenAutoPass(gameData);

            verifyNoInteractions(playerInputService, gameBroadcastService,
                    turnProgressionService, stateBasedActionService, effectResolutionService);
        }

        @Test
        void pendingMayAbilityIsProcessedAndCompletionStops() {
            gameData.pendingMayAbilities.add(pendingMayAbility());

            service.processMayAbilitiesThenAutoPass(gameData);

            verify(playerInputService).processNextMayAbility(gameData);
            verifyNoInteractions(gameBroadcastService, turnProgressionService, effectResolutionService);
        }

        @Test
        void resumesParkedResolutionBeforeClearingPriorityAndAutoPassing() {
            StackEntry parked = parkResolution();
            gameData.priorityPassedBy.add(playerId);

            service.processMayAbilitiesThenAutoPass(gameData);

            InOrder order = inOrder(playerInputService, effectResolutionService,
                    gameBroadcastService, turnProgressionService);
            order.verify(playerInputService).processNextMayAbility(gameData);
            order.verify(effectResolutionService).resolveEffectsFrom(gameData, parked, 2);
            order.verify(gameBroadcastService).broadcastGameState(gameData);
            order.verify(turnProgressionService).resolveAutoPass(gameData);
            assertThat(gameData.priorityPassedBy).isEmpty();
        }

        @Test
        void processesMayAbilityQueuedByResumedResolutionAndDoesNotAutoPass() {
            StackEntry parked = parkResolution();
            doAnswer(invocation -> {
                gameData.pendingMayAbilities.add(pendingMayAbility());
                return null;
            }).when(effectResolutionService).resolveEffectsFrom(gameData, parked, 2);

            service.processMayAbilitiesThenAutoPass(gameData);

            verify(playerInputService, times(2)).processNextMayAbility(gameData);
            verifyNoInteractions(gameBroadcastService, turnProgressionService);
        }

        @Test
        void stopsWhenResumedResolutionOpensAnotherInteraction() {
            StackEntry parked = parkResolution();
            doAnswer(invocation -> {
                gameData.interaction.beginInteraction(new PendingInteraction.Scry(playerId, List.of()));
                return null;
            }).when(effectResolutionService).resolveEffectsFrom(gameData, parked, 2);

            service.processMayAbilitiesThenAutoPass(gameData);

            verify(playerInputService).processNextMayAbility(gameData);
            verifyNoInteractions(gameBroadcastService, turnProgressionService);
        }
    }

    @Nested
    class SbaProcessMayAbilitiesThenAutoPass {

        @Test
        void performsStateBasedActionsThenRunsCommonCompletion() {
            gameData.priorityPassedBy.add(playerId);

            service.sbaProcessMayAbilitiesThenAutoPass(gameData);

            InOrder order = inOrder(stateBasedActionService, playerInputService,
                    gameBroadcastService, turnProgressionService);
            order.verify(stateBasedActionService).performStateBasedActions(gameData);
            order.verify(playerInputService).processNextMayAbility(gameData);
            order.verify(gameBroadcastService).broadcastGameState(gameData);
            order.verify(turnProgressionService).resolveAutoPass(gameData);
            assertThat(gameData.priorityPassedBy).isEmpty();
        }

        @Test
        void stopsWhenStateBasedActionsFinishTheGame() {
            doAnswer(invocation -> {
                gameData.status = GameStatus.FINISHED;
                return null;
            }).when(stateBasedActionService).performStateBasedActions(gameData);

            service.sbaProcessMayAbilitiesThenAutoPass(gameData);

            verify(stateBasedActionService).performStateBasedActions(gameData);
            verifyNoInteractions(playerInputService, gameBroadcastService,
                    turnProgressionService, effectResolutionService);
        }
    }

    @Nested
    class SbaMayAbilitiesThenBroadcastAutoPass {

        @Test
        void stopsWhenStateBasedActionsFinishTheGame() {
            doAnswer(invocation -> {
                gameData.status = GameStatus.FINISHED;
                return null;
            }).when(stateBasedActionService).performStateBasedActions(gameData);

            service.sbaMayAbilitiesThenBroadcastAutoPass(gameData);

            verify(stateBasedActionService).performStateBasedActions(gameData);
            verifyNoInteractions(playerInputService, gameBroadcastService,
                    turnProgressionService, effectResolutionService);
        }

        @Test
        void processesExistingMayAbilityWithoutResumingOrAutoPassing() {
            gameData.pendingMayAbilities.add(pendingMayAbility());
            parkResolution();

            service.sbaMayAbilitiesThenBroadcastAutoPass(gameData);

            verify(playerInputService).processNextMayAbility(gameData);
            verifyNoInteractions(gameBroadcastService, turnProgressionService, effectResolutionService);
        }

        @Test
        void resumesParkedResolutionBeforeBroadcastAndPreservesPriorityPasses() {
            StackEntry parked = parkResolution();
            gameData.priorityPassedBy.add(playerId);

            service.sbaMayAbilitiesThenBroadcastAutoPass(gameData);

            InOrder order = inOrder(stateBasedActionService, effectResolutionService,
                    gameBroadcastService, turnProgressionService);
            order.verify(stateBasedActionService).performStateBasedActions(gameData);
            order.verify(effectResolutionService).resolveEffectsFrom(gameData, parked, 2);
            order.verify(gameBroadcastService).broadcastGameState(gameData);
            order.verify(turnProgressionService).resolveAutoPass(gameData);
            assertThat(gameData.priorityPassedBy).containsExactly(playerId);
            verify(playerInputService, never()).processNextMayAbility(gameData);
        }

        @Test
        void processesMayAbilityQueuedByResumedResolutionAndDoesNotAutoPass() {
            StackEntry parked = parkResolution();
            doAnswer(invocation -> {
                gameData.pendingMayAbilities.add(pendingMayAbility());
                return null;
            }).when(effectResolutionService).resolveEffectsFrom(gameData, parked, 2);

            service.sbaMayAbilitiesThenBroadcastAutoPass(gameData);

            verify(playerInputService).processNextMayAbility(gameData);
            verifyNoInteractions(gameBroadcastService, turnProgressionService);
        }

        @Test
        void stopsWhenResumedResolutionOpensAnotherInteraction() {
            StackEntry parked = parkResolution();
            doAnswer(invocation -> {
                gameData.interaction.beginInteraction(new PendingInteraction.Scry(playerId, List.of()));
                return null;
            }).when(effectResolutionService).resolveEffectsFrom(gameData, parked, 2);

            service.sbaMayAbilitiesThenBroadcastAutoPass(gameData);

            verify(playerInputService, never()).processNextMayAbility(gameData);
            verifyNoInteractions(gameBroadcastService, turnProgressionService);
        }
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

    private Card namedCard(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }
}
