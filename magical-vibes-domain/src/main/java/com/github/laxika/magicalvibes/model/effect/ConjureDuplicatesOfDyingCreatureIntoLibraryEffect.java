package com.github.laxika.magicalvibes.model.effect;

/**
 * Conjures one duplicate per oil counter on the dying creature into its controller's library. The creature's card
 * is the stack entry's triggering card, and its oil counters those of the triggering permanent, as it last existed
 * on the battlefield (CR 603.10a).
 */
public record ConjureDuplicatesOfDyingCreatureIntoLibraryEffect() implements CardEffect {
}
