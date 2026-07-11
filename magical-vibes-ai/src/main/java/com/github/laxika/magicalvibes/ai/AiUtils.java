package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.GameData;

import java.util.UUID;

/**
 * Shared static utility methods for AI decision classes.
 */
class AiUtils {

    private AiUtils() {}

    static UUID getOpponentId(GameData gameData, UUID playerId) {
        for (UUID id : gameData.orderedPlayerIds) {
            if (!id.equals(playerId)) {
                return id;
            }
        }
        return null;
    }

    /**
     * Whether an AI seated as {@code aiPlayerId} must answer a choice addressed to
     * {@code choicePlayerId}. Normally only its own choices — but during a
     * Mindslaver-controlled turn the engine routes the controlled player's prompts
     * to the controlling player's connection and substitutes the acting player when
     * the answer arrives ({@code GameService.resolveActingPlayer}), so the
     * controller must answer those too.
     */
    static boolean isRespondingFor(GameData gameData, UUID aiPlayerId, UUID choicePlayerId) {
        if (aiPlayerId.equals(choicePlayerId)) {
            return true;
        }
        return aiPlayerId.equals(gameData.mindControllerPlayerId)
                && choicePlayerId != null
                && choicePlayerId.equals(gameData.mindControlledPlayerId);
    }
}
