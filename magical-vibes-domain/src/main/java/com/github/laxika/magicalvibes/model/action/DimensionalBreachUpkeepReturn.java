package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/**
 * Repeating upkeep trigger for Dimensional Breach's exiled permanent cards, controlled by the player who
 * controlled the spell as it resolved (CR 603.7d).
 */
public record DimensionalBreachUpkeepReturn(Card sourceCard, UUID controllerId) implements DelayedAction {
}
