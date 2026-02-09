package com.github.laxika.magicalvibes.networking;

import tools.jackson.databind.JsonNode;

public interface MessageHandler {

    void handleLogin(Connection connection, JsonNode jsonNode) throws Exception;

    void handleCreateGame(Connection connection, JsonNode jsonNode) throws Exception;

    void handleJoinGame(Connection connection, JsonNode jsonNode) throws Exception;

    void handlePassPriority(Connection connection, JsonNode jsonNode) throws Exception;

    void handleKeepHand(Connection connection, JsonNode jsonNode) throws Exception;

    void handleMulligan(Connection connection, JsonNode jsonNode) throws Exception;

    void handleBottomCards(Connection connection, JsonNode jsonNode) throws Exception;

    void handlePlayCard(Connection connection, JsonNode jsonNode) throws Exception;

    void handleTapPermanent(Connection connection, JsonNode jsonNode) throws Exception;

    void handleSetAutoStops(Connection connection, JsonNode jsonNode) throws Exception;

    void handleDeclareAttackers(Connection connection, JsonNode jsonNode) throws Exception;

    void handleDeclareBlockers(Connection connection, JsonNode jsonNode) throws Exception;

    void handleCardChosen(Connection connection, JsonNode jsonNode) throws Exception;

    void handleTimeout(Connection connection);

    void handleError(Connection connection, String message) throws Exception;
}
