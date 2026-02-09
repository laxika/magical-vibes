package com.github.laxika.magicalvibes.networking;

import com.github.laxika.magicalvibes.model.Player;

import java.util.Collection;

public interface SessionManager {

    void registerPlayer(Connection connection, Long userId, String username);

    void unregisterSession(String connectionId);

    Player getPlayer(String connectionId);

    Connection getConnectionByUserId(Long userId);

    void setInGame(String connectionId);

    Collection<Player> getLobbyPlayers();
}
