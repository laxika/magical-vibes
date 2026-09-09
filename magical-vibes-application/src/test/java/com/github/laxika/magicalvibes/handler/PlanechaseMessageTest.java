package com.github.laxika.magicalvibes.handler;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.Connection;
import com.github.laxika.magicalvibes.networking.message.*;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.websocket.WebSocketSessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanechaseMessageTest {
    @Mock private GameService games;
    @Mock private GameRegistry registry;
    @Mock private WebSocketSessionManager sessions;
    @Mock private Connection connection;
    @InjectMocks private GameMessageHandler handler;

    @BeforeEach
    void connection() {
        lenient().when(connection.getId()).thenReturn("connection");
        lenient().when(connection.isOpen()).thenReturn(true);
    }

    @Test
    void unauthenticatedRollIsRejected() throws Exception {
        handler.handleRollPlanarDie(connection, new RollPlanarDieRequest());
        verifyNoInteractions(games, registry);
        verify(connection).sendMessage(any(ErrorMessage.class));
    }

    @Test
    void rollUsesOnlyTheAuthenticatedPlayersGame() throws Exception {
        Player player = new Player(UUID.randomUUID(), "Player");
        GameData game = new GameData(UUID.randomUUID(), "Game", player.getId(), player.getUsername());
        when(sessions.getPlayer("connection")).thenReturn(player);
        when(registry.getGameForPlayer(player.getId())).thenReturn(game);
        handler.handleRollPlanarDie(connection, new RollPlanarDieRequest());
        verify(games).rollPlanarDie(game, player);
    }

    @Test
    void activationWithoutAGameIsRejected() throws Exception {
        Player player = new Player(UUID.randomUUID(), "Player");
        when(sessions.getPlayer("connection")).thenReturn(player);
        handler.handleActivatePlanarAbility(connection,
                new ActivatePlanarAbilityRequest(UUID.randomUUID(), 0, null, null, null));
        verifyNoInteractions(games);
        verify(connection).sendMessage(any(ErrorMessage.class));
    }

    @Test
    void aiPlanechaseCreationIsRejectedBeforeCreatingAGame() throws Exception {
        when(sessions.getPlayer("connection")).thenReturn(new Player(UUID.randomUUID(), "Player"));
        handler.handleCreateGame(connection, new CreateGameRequest("Game", "deck", true,
                null, null, false, null, true));
        verifyNoInteractions(games);
        verify(connection).sendMessage(any(ErrorMessage.class));
    }

    @Test
    void oldCreationPayloadDoesNotEnablePlanechase() {
        CreateGameRequest request = JsonMapper.builder().build().readValue(
                "{\"gameName\":\"Game\",\"deckId\":\"deck\"}", CreateGameRequest.class);
        assertThat(Boolean.TRUE.equals(request.planechase())).isFalse();
    }
}
