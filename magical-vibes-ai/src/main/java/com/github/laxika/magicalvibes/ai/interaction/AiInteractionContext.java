package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.ai.AiGameActions;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.networking.Connection;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;

import java.util.UUID;

/**
 * Everything an {@link AiInteractionStrategy} needs to compute and send its answer:
 * the game state, the AI's identity, and the wire-action adapter it answers through.
 */
public record AiInteractionContext(
        GameData gameData,
        UUID gameId,
        UUID aiPlayerId,
        GameQueryService gameQueryService,
        AiGameActions gameActions,
        Connection selfConnection
) {
}
