package com.github.laxika.magicalvibes.model.effect;

/**
 * Searches the controller's library for a creature card that shares a creature type with the
 * creature sacrificed to pay the same activated ability and has the required mana value, then
 * puts it onto the battlefield.
 */
public record SearchLibraryForCreatureSharingSacrificedCreatureTypeEffect() implements CardEffect {
}
