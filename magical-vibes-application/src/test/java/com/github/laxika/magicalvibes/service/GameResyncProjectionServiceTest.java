package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.networking.message.JoinGame;
import com.github.laxika.magicalvibes.service.event.GameEventDispatcher;
import com.github.laxika.magicalvibes.service.event.GameMutationCoordinator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GameResyncProjectionServiceTest {

    @Test
    void reconnectProjectionReadsCurrentAuthoritativeStateUnderTheGameMonitor() {
        UUID playerId = UUID.randomUUID();
        GameData gameData = new GameData(UUID.randomUUID(), "resync", playerId, "Player");
        gameData.status = GameStatus.MULLIGAN;
        gameData.playerNeedsToBottom.put(playerId, 2);

        JoinGame currentProjection = mock(JoinGame.class);
        GameViewProjectionFactory factory = mock(GameViewProjectionFactory.class);
        when(factory.getJoinGame(gameData, playerId)).thenAnswer(invocation -> {
            assertThat(Thread.holdsLock(gameData)).isTrue();
            assertThat(gameData.status).isEqualTo(GameStatus.MULLIGAN);
            assertThat(gameData.playerNeedsToBottom).containsEntry(playerId, 2);
            return currentProjection;
        });

        GameResyncProjectionService service =
                new GameResyncProjectionService(
                        factory,
                        mock(GameMessageTransport.class),
                        new GameMutationCoordinator(new GameEventDispatcher(List.of())));

        assertThat(service.currentState(gameData, playerId)).isSameAs(currentProjection);
        assertThat(gameData.domainActionSequence()).isZero();
        assertThat(gameData.domainStateVersion()).isZero();
        assertThat(gameData.domainEventSequence()).isZero();
    }
}
