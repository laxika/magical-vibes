package com.github.laxika.magicalvibes.networking;

import com.github.laxika.magicalvibes.model.Player;

import java.util.Collection;
import java.util.UUID;

public interface SessionManager {

    void registerPlayer(Connection connection, UUID userId, String username);

    void unregisterSession(String connectionId);

    Player getPlayer(String connectionId);

    Connection getConnectionByUserId(UUID userId);

    void setInGame(String connectionId);

    Collection<Player> getLobbyPlayers();

    void sendToPlayer(UUID playerId, Object message);

    void sendToPlayers(Collection<UUID> playerIds, Object message);
}
