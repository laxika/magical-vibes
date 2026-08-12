package com.github.laxika.magicalvibes.service.cast;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;

import java.util.UUID;

/**
 * The spell whose effective cast cost is being computed: the game state, the player
 * casting it, the card itself, and whether a flashback cost is being paid.
 */
public record CostModificationContext(GameData gameData, UUID castingPlayerId, Card spell,
                                      boolean flashbackCost, int xValue) {

    public CostModificationContext(GameData gameData, UUID castingPlayerId, Card spell) {
        this(gameData, castingPlayerId, spell, false, 0);
    }

    public CostModificationContext(GameData gameData, UUID castingPlayerId, Card spell,
                                   boolean flashbackCost) {
        this(gameData, castingPlayerId, spell, flashbackCost, 0);
    }
}
