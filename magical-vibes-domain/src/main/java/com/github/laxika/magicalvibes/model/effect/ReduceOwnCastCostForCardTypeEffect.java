package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

/**
 * Reduces the controller's spells of the given card types by the given amount.
 * Applied as a static effect from a permanent on the battlefield (e.g. Heartless Summoning).
 */
public record ReduceOwnCastCostForCardTypeEffect(Set<CardType> affectedTypes, int amount) implements CardEffect {
}
