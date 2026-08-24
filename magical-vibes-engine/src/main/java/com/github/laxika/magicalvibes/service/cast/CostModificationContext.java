package com.github.laxika.magicalvibes.service.cast;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Zone;

import java.util.UUID;

/**
 * The spell whose effective cast cost is being computed: the game state, the player
 * casting it, the card itself, whether a flashback cost is being paid, and the zone the spell is
 * being cast from.
 */
public record CostModificationContext(GameData gameData, UUID castingPlayerId, Card spell,
                                      boolean flashbackCost, int xValue, Zone sourceZone) {

    public CostModificationContext(GameData gameData, UUID castingPlayerId, Card spell) {
        this(gameData, castingPlayerId, spell, false, 0, Zone.HAND);
    }

    public CostModificationContext(GameData gameData, UUID castingPlayerId, Card spell,
                                   boolean flashbackCost) {
        this(gameData, castingPlayerId, spell, flashbackCost, 0, Zone.HAND);
    }

    public CostModificationContext(GameData gameData, UUID castingPlayerId, Card spell,
                                   boolean flashbackCost, int xValue) {
        this(gameData, castingPlayerId, spell, flashbackCost, xValue, Zone.HAND);
    }
}
