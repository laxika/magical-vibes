package com.github.laxika.magicalvibes.model.effect;

/**
 * Conjures a duplicate of the stack entry's triggering permanent (the enlisted creature: where it is as the ability
 * resolves, else as it last existed on the battlefield) into a random position among the top five cards of the
 * controller's library; the duplicate perpetually gets +1/+0 and gains haste.
 */
public record ConjureDuplicateOfEnlistedCreatureIntoTopFiveEffect() implements CardEffect {
}
