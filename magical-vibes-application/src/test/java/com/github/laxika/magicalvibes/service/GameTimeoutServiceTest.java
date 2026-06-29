package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.networking.Connection;
import com.github.laxika.magicalvibes.websocket.WebSocketSessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GameTimeoutServiceTest {

    @Mock private GameRegistry gameRegistry;
    @Mock private GameOutcomeService gameOutcomeService;
    @Mock private WebSocketSessionManager sessionManager;
    @Mock private ScheduledExecutorService scheduler;

    private GameTimeoutService svc;
    private GameData gameData;
    private UUID player1Id;
    private UUID player2Id;
    private Connection player1Connection;
    private Connection player2Connection;

    @BeforeEach
    void setUp() {
        svc = new GameTimeoutService(
                gameRegistry, gameOutcomeService, sessionManager,
                Duration.ofMinutes(5), Duration.ofMinutes(15), scheduler);

        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gameData = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gameData.playerIds.add(player1Id);
        gameData.playerIds.add(player2Id);
        gameData.orderedPlayerIds.add(player1Id);
        gameData.orderedPlayerIds.add(player2Id);
        gameData.status = GameStatus.RUNNING;

        player1Connection = mock(Connection.class);
        when(player1Connection.getId()).thenReturn("conn-p1");
        player2Connection = mock(Connection.class);
        when(player2Connection.getId()).thenReturn("conn-p2");
    }

    private void givenOnlyPlayer2Connected() {
        when(sessionManager.getConnectionByUserId(player1Id)).thenReturn(null);
        when(sessionManager.getConnectionByUserId(player2Id)).thenReturn(player2Connection);
        when(sessionManager.isInGame("conn-p2")).thenReturn(true);
    }

    private void givenBothDisconnected() {
        when(sessionManager.getConnectionByUserId(player1Id)).thenReturn(null);
        when(sessionManager.getConnectionByUserId(player2Id)).thenReturn(null);
    }

    private ScheduledFuture<?> stubScheduleReturnsFuture() {
        ScheduledFuture<?> future = mock(ScheduledFuture.class);
        when(scheduler.schedule(any(Runnable.class), anyLong(), eq(TimeUnit.MILLISECONDS)))
                .thenAnswer(inv -> future);
        return future;
    }

    @Test
    void disconnectArmsSingleGoneTimerWhenOpponentConnected() {
        when(gameRegistry.getGameForPlayer(player1Id)).thenReturn(gameData);
        givenOnlyPlayer2Connected();
        stubScheduleReturnsFuture();

        svc.onPlayerDisconnect(player1Id);

        verify(scheduler).schedule(any(Runnable.class), eq(Duration.ofMinutes(15).toMillis()), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    void disconnectArmsBothGoneTimerWhenAllOpponentsAlsoGone() {
        when(gameRegistry.getGameForPlayer(player1Id)).thenReturn(gameData);
        givenBothDisconnected();
        stubScheduleReturnsFuture();

        svc.onPlayerDisconnect(player1Id);

        verify(scheduler).schedule(any(Runnable.class), eq(Duration.ofMinutes(5).toMillis()), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    void reconnectCancelsThatPlayersSingleGoneTimer() {
        when(gameRegistry.getGameForPlayer(player1Id)).thenReturn(gameData);
        givenOnlyPlayer2Connected();
        ScheduledFuture<?> future = stubScheduleReturnsFuture();

        svc.onPlayerDisconnect(player1Id);
        // Now player1 reconnects
        when(sessionManager.getConnectionByUserId(player1Id)).thenReturn(player1Connection);
        when(sessionManager.isInGame("conn-p1")).thenReturn(true);

        svc.onPlayerReconnect(player1Id);

        verify(future).cancel(false);
    }

    @Test
    void reconnectCancelsBothGoneTimerAndArmsSingleGoneForStillAbsentOpponent() {
        when(gameRegistry.getGameForPlayer(player1Id)).thenReturn(gameData);
        when(gameRegistry.getGameForPlayer(player2Id)).thenReturn(gameData);
        givenBothDisconnected();
        ScheduledFuture<?> bothGone = stubScheduleReturnsFuture();

        svc.onPlayerDisconnect(player1Id);
        // Both-gone armed (5-min). Now player2 reconnects.
        when(sessionManager.getConnectionByUserId(player2Id)).thenReturn(player2Connection);
        when(sessionManager.isInGame("conn-p2")).thenReturn(true);

        svc.onPlayerReconnect(player2Id);

        verify(bothGone).cancel(false);
        // The reconnect should also arm a single-gone timer for the still-absent player1.
        // That's the 2nd schedule call (the 1st was both-gone).
        verify(scheduler, times(2)).schedule(any(Runnable.class), anyLong(), eq(TimeUnit.MILLISECONDS));
        verify(scheduler).schedule(any(Runnable.class), eq(Duration.ofMinutes(15).toMillis()), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    void singleGoneTimerFiringDeclaresOpponentTheWinner() {
        when(gameRegistry.getGameForPlayer(player1Id)).thenReturn(gameData);
        when(gameRegistry.get(gameData.id)).thenReturn(gameData);
        givenOnlyPlayer2Connected();
        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        stubScheduleReturnsFuture();

        svc.onPlayerDisconnect(player1Id);

        verify(scheduler).schedule(captor.capture(), eq(Duration.ofMinutes(15).toMillis()), eq(TimeUnit.MILLISECONDS));
        captor.getValue().run();

        verify(gameOutcomeService).declareWinner(gameData, player2Id);
    }

    @Test
    void singleGoneTimerDoesNotFireIfPlayerReconnectedBeforeFiring() {
        when(gameRegistry.getGameForPlayer(player1Id)).thenReturn(gameData);
        when(gameRegistry.get(gameData.id)).thenReturn(gameData);
        givenOnlyPlayer2Connected();
        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        stubScheduleReturnsFuture();

        svc.onPlayerDisconnect(player1Id);
        verify(scheduler).schedule(captor.capture(), anyLong(), any());

        // Player1 reconnects, then the (already-fired-before-cancel) runnable runs.
        when(sessionManager.getConnectionByUserId(player1Id)).thenReturn(player1Connection);
        when(sessionManager.isInGame("conn-p1")).thenReturn(true);

        captor.getValue().run();

        verify(gameOutcomeService, never()).declareWinner(any(), any());
    }

    @Test
    void bothGoneTimerFiringRemovesCasualGameWithoutWinner() {
        when(gameRegistry.getGameForPlayer(player1Id)).thenReturn(gameData);
        when(gameRegistry.get(gameData.id)).thenReturn(gameData);
        givenBothDisconnected();
        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        stubScheduleReturnsFuture();

        svc.onPlayerDisconnect(player1Id);
        verify(scheduler).schedule(captor.capture(), eq(Duration.ofMinutes(5).toMillis()), eq(TimeUnit.MILLISECONDS));

        captor.getValue().run();

        verify(gameRegistry).remove(gameData.id);
        verify(gameOutcomeService, never()).declareWinner(any(), any());
        assertThat(gameData.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    void bothGoneTimerFiringInTournamentDeclaresRandomWinner() {
        gameData.draftId = UUID.randomUUID();
        when(gameRegistry.getGameForPlayer(player1Id)).thenReturn(gameData);
        when(gameRegistry.get(gameData.id)).thenReturn(gameData);
        givenBothDisconnected();
        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        stubScheduleReturnsFuture();

        svc.onPlayerDisconnect(player1Id);
        verify(scheduler).schedule(captor.capture(), eq(Duration.ofMinutes(5).toMillis()), eq(TimeUnit.MILLISECONDS));

        captor.getValue().run();

        ArgumentCaptor<UUID> winnerCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(gameOutcomeService).declareWinner(eq(gameData), winnerCaptor.capture());
        assertThat(winnerCaptor.getValue()).isIn(player1Id, player2Id);
        verify(gameRegistry, never()).remove(gameData.id); // declareWinner does that itself
    }

    @Test
    void leavingVsAiGameClosesImmediatelyWithoutTimer() {
        Connection aiConnection = mock(Connection.class);
        when(aiConnection.getId()).thenReturn("ai-" + gameData.id);
        when(sessionManager.getConnectionByUserId(player1Id)).thenReturn(player1Connection);
        when(sessionManager.getConnectionByUserId(player2Id)).thenReturn(aiConnection);
        when(gameRegistry.getGameForPlayer(player1Id)).thenReturn(gameData);

        svc.onPlayerDisconnect(player1Id);

        verify(gameRegistry).remove(gameData.id);
        verify(sessionManager).unregisterSession("ai-" + gameData.id);
        verifyNoInteractions(scheduler);
        assertThat(gameData.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    void simulationGamesNeverArmTimers() {
        gameData.simulation = true;
        when(gameRegistry.getGameForPlayer(player1Id)).thenReturn(gameData);

        svc.onPlayerDisconnect(player1Id);

        verifyNoInteractions(scheduler);
        verifyNoInteractions(gameOutcomeService);
    }

    @Test
    void onGameFinishedCancelsBothTimerKinds() {
        when(gameRegistry.getGameForPlayer(player1Id)).thenReturn(gameData);
        givenOnlyPlayer2Connected();
        ScheduledFuture<?> singleGone = stubScheduleReturnsFuture();

        svc.onPlayerDisconnect(player1Id);
        svc.onGameFinished(gameData);

        verify(singleGone).cancel(false);
    }

    @Test
    void waitingGameDoesNotArmTimers() {
        gameData.status = GameStatus.WAITING;
        when(gameRegistry.getGameForPlayer(player1Id)).thenReturn(gameData);

        svc.onPlayerDisconnect(player1Id);

        verifyNoInteractions(scheduler);
    }

    @Test
    void finishedGameDoesNotArmTimers() {
        gameData.status = GameStatus.FINISHED;
        when(gameRegistry.getGameForPlayer(player1Id)).thenReturn(gameData);

        svc.onPlayerDisconnect(player1Id);

        verifyNoInteractions(scheduler);
    }

    @Test
    void disconnectWithNoActiveGameIsNoOp() {
        when(gameRegistry.getGameForPlayer(player1Id)).thenReturn(null);

        svc.onPlayerDisconnect(player1Id);

        verifyNoInteractions(scheduler);
        verifyNoInteractions(gameOutcomeService);
    }

    @Test
    void mulliganPhaseGameStillArmsTimers() {
        gameData.status = GameStatus.MULLIGAN;
        when(gameRegistry.getGameForPlayer(player1Id)).thenReturn(gameData);
        givenOnlyPlayer2Connected();
        stubScheduleReturnsFuture();

        svc.onPlayerDisconnect(player1Id);

        verify(scheduler, atLeastOnce()).schedule(any(Runnable.class), anyLong(), eq(TimeUnit.MILLISECONDS));
    }
}
