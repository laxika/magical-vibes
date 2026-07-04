package com.github.laxika.magicalvibes.service.cast;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;

import java.util.UUID;

/**
 * The spell whose effective cast cost is being computed: the game state, the player
 * casting it, and the card itself.
 */
public record CostModificationContext(GameData gameData, UUID castingPlayerId, Card spell) {
}
