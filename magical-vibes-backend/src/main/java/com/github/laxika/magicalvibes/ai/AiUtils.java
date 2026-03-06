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
}
