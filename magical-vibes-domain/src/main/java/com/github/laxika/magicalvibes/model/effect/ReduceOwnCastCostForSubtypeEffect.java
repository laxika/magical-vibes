package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.Set;

/**
 * Reduces the controller's spells with the given subtypes by the given amount of generic mana.
 * Applied as a static effect from a permanent on the battlefield (e.g. Danitha Capashen, Paragon).
 */
public record ReduceOwnCastCostForSubtypeEffect(Set<CardSubtype> affectedSubtypes, int amount) implements CardEffect {
}
