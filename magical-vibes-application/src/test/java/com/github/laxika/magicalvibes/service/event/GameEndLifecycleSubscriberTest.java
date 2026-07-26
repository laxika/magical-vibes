package com.github.laxika.magicalvibes.service.event;

import com.github.laxika.magicalvibes.model.DraftData;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.event.GameEventAudience;
import com.github.laxika.magicalvibes.model.event.GameEventBatch;
import com.github.laxika.magicalvibes.model.event.GameEventEnvelope;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.networking.Connection;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.service.DraftRegistry;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.GameTimeoutService;
import com.github.laxika.magicalvibes.service.TournamentResultHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GameEndLifecycleSubscriberTest {

    @Test
    void gameEndFactClosesAndUnregistersAiThenCleansRuntimeGame() throws Exception {
        UUID humanId = UUID.randomUUID();
        UUID aiId = UUID.randomUUID();
        UUID winnerId = humanId;
        GameData gameData = new GameData(UUID.randomUUID(), "end", humanId, "Human");
        gameData.playerIds.addAll(List.of(humanId, aiId));
        gameData.orderedPlayerIds.addAll(List.of(humanId, aiId));

        GameRegistry games = mock(GameRegistry.class);
        when(games.get(gameData.id)).thenReturn(gameData);
        DraftRegistry drafts = mock(DraftRegistry.class);
        SessionManager sessions = mock(SessionManager.class);
        Connection ai = mock(Connection.class);
        when(ai.getId()).thenReturn("ai-" + gameData.id);
        when(sessions.getConnectionByUserId(aiId)).thenReturn(ai);

        GameTimeoutService timeouts = mock(GameTimeoutService.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<GameTimeoutService> timeoutProvider = mock(ObjectProvider.class);
        when(timeoutProvider.getIfAvailable()).thenReturn(timeouts);
        @SuppressWarnings("unchecked")
        ObjectProvider<TournamentResultHandler> tournamentProvider = mock(ObjectProvider.class);

        GameEndLifecycleSubscriber subscriber = new GameEndLifecycleSubscriber(
                games, drafts, tournamentProvider, timeoutProvider, sessions);
        subscriber.onGameEvents(batch(gameData,
                new GameEventFact.GameEnded(GameEventFact.GameResult.WIN, winnerId)));

        verify(ai).close();
        verify(sessions).unregisterSession(ai.getId());
        verify(timeouts).onGameFinished(gameData);
        verify(games).remove(gameData.id);
    }

    @Test
    void tournamentWinAdvancesBracketAfterTerminalFact() {
        UUID winnerId = UUID.randomUUID();
        GameData gameData = new GameData(UUID.randomUUID(), "tournament", winnerId, "Winner");
        gameData.playerIds.add(winnerId);
        gameData.orderedPlayerIds.add(winnerId);
        gameData.draftId = UUID.randomUUID();

        GameRegistry games = mock(GameRegistry.class);
        when(games.get(gameData.id)).thenReturn(gameData);
        DraftRegistry drafts = mock(DraftRegistry.class);
        DraftData draftData = mock(DraftData.class);
        when(drafts.get(gameData.draftId)).thenReturn(draftData);
        TournamentResultHandler tournament = mock(TournamentResultHandler.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<TournamentResultHandler> tournamentProvider = mock(ObjectProvider.class);
        when(tournamentProvider.getIfAvailable()).thenReturn(tournament);
        @SuppressWarnings("unchecked")
        ObjectProvider<GameTimeoutService> timeoutProvider = mock(ObjectProvider.class);

        GameEndLifecycleSubscriber subscriber = new GameEndLifecycleSubscriber(
                games, drafts, tournamentProvider, timeoutProvider, mock(SessionManager.class));
        subscriber.onGameEvents(batch(gameData,
                new GameEventFact.GameEnded(GameEventFact.GameResult.WIN, winnerId)));

        verify(tournament).handleGameFinished(draftData, winnerId);
        verify(games).remove(gameData.id);
    }

    private static GameEventBatch batch(GameData gameData, GameEventFact.GameEnded ended) {
        UUID actionId = UUID.randomUUID();
        GameEventEnvelope envelope = new GameEventEnvelope(
                gameData.id,
                1,
                actionId,
                1,
                ended.kind(),
                ended,
                GameEventAudience.allPlayers());
        return new GameEventBatch(
                gameData.id,
                actionId,
                1,
                GameEventBatch.DispatchMode.LIVE,
                List.of(envelope));
    }
}
