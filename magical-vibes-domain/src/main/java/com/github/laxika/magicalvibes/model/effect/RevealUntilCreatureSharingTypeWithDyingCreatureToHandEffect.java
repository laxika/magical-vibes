package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals until a creature card sharing a creature type with the creature that died is found,
 * putting that card into its controller's hand and the other revealed cards on the bottom of the
 * library in a random order. The creature that died is the stack entry's triggering permanent, as it last existed
 * on the battlefield (CR 603.10a).
 */
public record RevealUntilCreatureSharingTypeWithDyingCreatureToHandEffect() implements CardEffect {
}
