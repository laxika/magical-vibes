package com.github.laxika.magicalvibes.service.event;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.event.GameEventBatch;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.model.effect.KarnRestartGameEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.service.LichsMirrorResetService;
import com.github.laxika.magicalvibes.service.MulliganService;
import com.github.laxika.magicalvibes.service.ReconnectionService;
import com.github.laxika.magicalvibes.service.StackResolutionService;
import com.github.laxika.magicalvibes.service.ability.AbilityActivationService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.CombatService;
import com.github.laxika.magicalvibes.service.combat.CombatAttackService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.KarnRestartGameEffectHandler;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.spell.SpellCastingService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import com.github.laxika.magicalvibes.service.turn.AutoPassService;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.service.turn.UntapStepService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GameLifecycleEventSequenceTest {

    private UUID player1Id;
    private UUID player2Id;
    private Player player1;
    private Player player2;
    private GameData gameData;
    private RecordingSubscriber events;
    private GameMutationCoordinator coordinator;
    private GameBroadcastService broadcasts;
    private GameQueryService gameQueryService;
    private GameOutcomeService outcomeService;

    @BeforeEach
    void setUp() {
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        player1 = new Player(player1Id, "Player 1");
        player2 = new Player(player2Id, "Player 2");
        gameData = gameData();
        events = new RecordingSubscriber();
        coordinator = new GameMutationCoordinator(new GameEventDispatcher(List.of(events)));
        broadcasts = mock(GameBroadcastService.class);
        gameQueryService = mock(GameQueryService.class);
        outcomeService = new GameOutcomeService(
                gameQueryService,
                broadcasts,
                mock(LichsMirrorResetService.class),
                coordinator);
    }

    @Test
    void mulliganBottomCardsAndGameStartPreserveCanonicalOrder() {
        gameData.status = GameStatus.MULLIGAN;
        gameData.startingPlayerId = player1Id;
        seedOpeningHand(player1Id);
        seedOpeningHand(player2Id);

        TurnProgressionService turns = mock(TurnProgressionService.class);
        MulliganService mulligans = new MulliganService(
                broadcasts,
                turns,
                mock(BattlefieldEntryService.class),
                mock(PlayerInputService.class),
                coordinator);

        coordinator.mutate(gameData, () -> mulligans.mulligan(gameData, player1));
        assertThat(lastFacts())
                .extracting(GameEventFact::kind)
                .containsExactly(
                        com.github.laxika.magicalvibes.model.event.GameEventKind.MULLIGAN_RESOLVED,
                        com.github.laxika.magicalvibes.model.event.GameEventKind.STATE_INVALIDATED,
                        com.github.laxika.magicalvibes.model.event.GameEventKind.DECISION_REQUESTED);

        coordinator.mutate(gameData, () -> mulligans.keepHand(gameData, player1));
        assertThat(lastFacts())
                .extracting(GameEventFact::kind)
                .containsExactly(
                        com.github.laxika.magicalvibes.model.event.GameEventKind.MULLIGAN_RESOLVED,
                        com.github.laxika.magicalvibes.model.event.GameEventKind.STATE_INVALIDATED,
                        com.github.laxika.magicalvibes.model.event.GameEventKind.DECISION_REQUESTED);
        assertThat(lastFacts().get(2))
                .isInstanceOfSatisfying(GameEventFact.DecisionRequested.class,
                        decision -> assertThat(decision.decisionKind())
                                .isEqualTo(GameEventFact.DecisionKind.CARDS_TO_BOTTOM));

        coordinator.mutate(gameData, () -> mulligans.bottomCards(gameData, player1, List.of(0)));
        assertThat(gameData.playerNeedsToBottom).doesNotContainKey(player1Id);
        assertThat(lastFacts())
                .extracting(GameEventFact::kind)
                .containsExactly(
                        com.github.laxika.magicalvibes.model.event.GameEventKind.STATE_INVALIDATED);

        coordinator.mutate(gameData, () -> mulligans.keepHand(gameData, player2));
        assertThat(gameData.status).isEqualTo(GameStatus.RUNNING);
        assertThat(lastFacts())
                .extracting(GameEventFact::kind)
                .containsExactly(
                        com.github.laxika.magicalvibes.model.event.GameEventKind.MULLIGAN_RESOLVED,
                        com.github.laxika.magicalvibes.model.event.GameEventKind.STATE_INVALIDATED);
    }

    @Test
    void surrenderEmitsOneTerminalFactAfterAuthoritativeWinnerIsFinal() {
        gameData.status = GameStatus.RUNNING;
        when(gameQueryService.getOpponentId(gameData, player1Id)).thenReturn(player2Id);
        when(gameQueryService.getOpponentId(gameData, player2Id)).thenReturn(player1Id);

        GameService gameService = new GameService(
                mock(GameRegistry.class),
                gameQueryService,
                broadcasts,
                mock(CombatService.class),
                mock(TurnProgressionService.class),
                new InteractionHandlerRegistry(),
                mock(SpellCastingService.class),
                mock(StackResolutionService.class),
                mock(AbilityActivationService.class),
                mock(MulliganService.class),
                mock(ReconnectionService.class),
                mock(ExileSupport.class),
                outcomeService,
                coordinator);

        gameService.surrender(gameData, player1);

        assertThat(gameData.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gameData.gameResult).isEqualTo(GameEventFact.GameResult.WIN);
        assertThat(gameData.winnerPlayerId).isEqualTo(player2Id);
        assertThat(lastFacts())
                .singleElement()
                .isEqualTo(new GameEventFact.GameEnded(
                        GameEventFact.GameResult.WIN, player2Id));
    }

    @Test
    void turnTransitionAndAutoPassExposeOnlyTheFinalPriorityWindow() {
        gameData.status = GameStatus.RUNNING;
        gameData.activePlayerId = player1Id;
        gameData.currentStep = TurnStep.UPKEEP;
        gameData.playerAutoStopSteps.put(player1Id, java.util.Set.of(TurnStep.DRAW));
        gameData.playerBattlefields.put(player1Id, gameData.newBattlefieldList());
        gameData.playerBattlefields.put(player2Id, gameData.newBattlefieldList());
        when(gameQueryService.getPriorityPlayerId(gameData)).thenReturn(player1Id);
        when(broadcasts.getPlayableCardIndices(gameData, player1Id)).thenReturn(List.of());

        StepTriggerService stepTriggers = mock(StepTriggerService.class);
        CombatService combat = mock(CombatService.class);
        AutoPassService autoPass = new AutoPassService(
                gameQueryService,
                broadcasts,
                mock(TriggerCollectionService.class),
                mock(StackResolutionService.class),
                stepTriggers,
                mock(CombatAttackService.class),
                mock(PotentialManaService.class),
                coordinator);
        TurnProgressionService turns = new TurnProgressionService(
                combat,
                broadcasts,
                mock(PlayerInputService.class),
                mock(TurnCleanupService.class),
                mock(UntapStepService.class),
                stepTriggers,
                autoPass,
                coordinator);

        coordinator.mutate(gameData, () -> {
            turns.advanceStep(gameData);
            turns.resolveAutoPass(gameData);
        });

        assertThat(gameData.currentStep).isEqualTo(TurnStep.DRAW);
        assertThat(lastFacts())
                .singleElement()
                .isInstanceOf(GameEventFact.StateInvalidated.class);
    }

    @Test
    void stateBasedPlayerLossAndDrawEachEmitExactlyOneFinalResult() {
        gameData.status = GameStatus.RUNNING;
        gameData.playerLifeTotals.put(player1Id, 0);
        gameData.playerLifeTotals.put(player2Id, 20);
        when(gameQueryService.canPlayerLoseGame(gameData, player1Id)).thenReturn(true);
        when(gameQueryService.canPlayerLoseFromLife(gameData, player1Id)).thenReturn(true);
        when(gameQueryService.getOpponentId(gameData, player1Id)).thenReturn(player2Id);

        coordinator.mutate(gameData, () -> outcomeService.checkWinCondition(gameData));
        coordinator.mutate(gameData, () -> outcomeService.checkWinCondition(gameData));

        assertThat(events.batches)
                .flatExtracting(GameEventBatch::events)
                .extracting(envelope -> envelope.fact())
                .filteredOn(GameEventFact.GameEnded.class::isInstance)
                .containsExactly(new GameEventFact.GameEnded(
                        GameEventFact.GameResult.WIN, player2Id));

        GameData drawGame = gameData();
        RecordingSubscriber drawEvents = new RecordingSubscriber();
        GameMutationCoordinator drawCoordinator =
                new GameMutationCoordinator(new GameEventDispatcher(List.of(drawEvents)));
        GameOutcomeService drawOutcome = new GameOutcomeService(
                mock(GameQueryService.class),
                broadcasts,
                mock(LichsMirrorResetService.class),
                drawCoordinator);

        drawCoordinator.mutate(drawGame, () -> drawOutcome.declareDraw(drawGame));
        drawCoordinator.mutate(drawGame, () -> drawOutcome.declareDraw(drawGame));

        assertThat(drawGame.gameResult).isEqualTo(GameEventFact.GameResult.DRAW);
        assertThat(drawEvents.batches)
                .flatExtracting(GameEventBatch::events)
                .extracting(envelope -> envelope.fact())
                .filteredOn(GameEventFact.GameEnded.class::isInstance)
                .containsExactly(new GameEventFact.GameEnded(
                        GameEventFact.GameResult.DRAW, null));
    }

    @Test
    void restartResetsMulliganDecisionsAndEmitsStateBeforeWakeup() {
        seedRuntimeZones(player1Id);
        seedRuntimeZones(player2Id);
        gameData.status = GameStatus.RUNNING;
        gameData.playerKeptHand.add(player1Id);
        gameData.playerNeedsToBottom.put(player1Id, 1);
        gameData.playerBottomDecisionIds.put(player1Id, UUID.randomUUID());

        KarnRestartGameEffectHandler handler =
                new KarnRestartGameEffectHandler(broadcasts, coordinator);
        StackEntry entry = new StackEntry(
                StackEntryType.ACTIVATED_ABILITY,
                new Card(),
                player1Id,
                "Restart",
                List.of(new KarnRestartGameEffect()));

        coordinator.mutate(gameData,
                () -> handler.resolve(gameData, entry, new KarnRestartGameEffect()));

        assertThat(gameData.status).isEqualTo(GameStatus.MULLIGAN);
        assertThat(gameData.playerKeptHand).isEmpty();
        assertThat(gameData.playerNeedsToBottom).isEmpty();
        assertThat(gameData.playerBottomDecisionIds).isEmpty();
        assertThat(gameData.playerMulliganDecisionIds).containsKeys(player1Id, player2Id);
        assertThat(lastFacts())
                .extracting(GameEventFact::kind)
                .containsExactly(
                        com.github.laxika.magicalvibes.model.event.GameEventKind.STATE_INVALIDATED,
                        com.github.laxika.magicalvibes.model.event.GameEventKind.MULLIGAN_RESOLVED,
                        com.github.laxika.magicalvibes.model.event.GameEventKind.DECISION_REQUESTED,
                        com.github.laxika.magicalvibes.model.event.GameEventKind.DECISION_REQUESTED);
    }

    @Test
    void reconnectReplaysEveryPendingDecisionWithItsStableIdentity() {
        ReconnectionService reconnect = new ReconnectionService(coordinator);
        List<PendingInteraction> interactions = List.of(
                new PendingInteraction.PermanentChoice(
                        player1Id, List.of(), List.of(), null, "Choose."),
                new PendingInteraction.AttackerDeclaration(player1Id),
                new PendingInteraction.BlockerDeclaration(player1Id),
                new PendingInteraction.CombatDamageAssignment(
                        player1Id, 0, UUID.randomUUID(), "Attacker", 3,
                        List.of(), false, false, false));
        List<GameEventFact.DecisionKind> kinds = List.of(
                GameEventFact.DecisionKind.INTERACTION,
                GameEventFact.DecisionKind.ATTACKER_DECLARATION,
                GameEventFact.DecisionKind.BLOCKER_DECLARATION,
                GameEventFact.DecisionKind.COMBAT_DAMAGE_ASSIGNMENT);

        for (int i = 0; i < interactions.size(); i++) {
            gameData.interaction.beginInteraction(interactions.get(i));
            UUID decisionId = gameData.interaction.activeDecisionId();
            coordinator.mutate(gameData,
                    () -> reconnect.resendAwaitingInput(gameData, player1Id));
            assertReplay(lastFacts(), decisionId, kinds.get(i));
            gameData.interaction.clearAwaitingInput();
        }

        UUID bottomId = UUID.randomUUID();
        gameData.playerNeedsToBottom.put(player1Id, 2);
        gameData.playerBottomDecisionIds.put(player1Id, bottomId);
        coordinator.mutate(gameData,
                () -> reconnect.resendAwaitingInput(gameData, player1Id));
        assertReplay(lastFacts(), bottomId, GameEventFact.DecisionKind.CARDS_TO_BOTTOM);

        gameData.playerNeedsToBottom.clear();
        gameData.playerBottomDecisionIds.clear();
        gameData.status = GameStatus.MULLIGAN;
        UUID mulliganId = UUID.randomUUID();
        gameData.playerMulliganDecisionIds.put(player1Id, mulliganId);
        coordinator.mutate(gameData,
                () -> reconnect.resendAwaitingInput(gameData, player1Id));
        assertReplay(lastFacts(), mulliganId, GameEventFact.DecisionKind.MULLIGAN);
    }

    private void assertReplay(
            List<GameEventFact> facts,
            UUID decisionId,
            GameEventFact.DecisionKind kind) {
        assertThat(facts)
                .singleElement()
                .isInstanceOfSatisfying(GameEventFact.DecisionRequested.class, decision -> {
                    assertThat(decision.decisionId()).isEqualTo(decisionId);
                    assertThat(decision.decisionKind()).isEqualTo(kind);
                    assertThat(decision.delivery())
                            .isEqualTo(GameEventFact.DecisionDelivery.REPLAY_REQUESTED);
                });
    }

    private List<GameEventFact> lastFacts() {
        return events.batches.getLast().events().stream()
                .map(envelope -> envelope.fact())
                .toList();
    }

    private GameData gameData() {
        GameData data = new GameData(UUID.randomUUID(), "lifecycle", player1Id, "Player 1");
        data.playerIds.addAll(List.of(player1Id, player2Id));
        data.orderedPlayerIds.addAll(List.of(player1Id, player2Id));
        data.playerIdToName.put(player1Id, "Player 1");
        data.playerIdToName.put(player2Id, "Player 2");
        data.playerLifeTotals.put(player1Id, 20);
        data.playerLifeTotals.put(player2Id, 20);
        return data;
    }

    private void seedOpeningHand(UUID playerId) {
        gameData.playerHands.put(playerId, new ArrayList<>(cards(7)));
        gameData.playerDecks.put(playerId, new ArrayList<>(cards(10)));
        gameData.mulliganCounts.put(playerId, 0);
        gameData.playerMulliganDecisionIds.put(playerId, UUID.randomUUID());
    }

    private void seedRuntimeZones(UUID playerId) {
        gameData.playerHands.put(playerId, new ArrayList<>());
        gameData.playerDecks.put(playerId, new ArrayList<>(cards(10)));
        gameData.playerGraveyards.put(playerId, new ArrayList<>());
        gameData.playerBattlefields.put(playerId, gameData.newBattlefieldList());
        gameData.playerManaPools.put(playerId, new ManaPool());
    }

    private static List<Card> cards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Card());
        }
        return cards;
    }

    private static final class RecordingSubscriber implements GameEventSubscriber {
        private final List<GameEventBatch> batches = new ArrayList<>();

        @Override
        public void onGameEvents(GameEventBatch batch) {
            batches.add(batch);
        }
    }
}
