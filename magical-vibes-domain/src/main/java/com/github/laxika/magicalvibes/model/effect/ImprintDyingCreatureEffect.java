package com.github.laxika.magicalvibes.model.effect;

/**
 * Imprint trigger effect for Mimic Vat: exile a dying creature card, the stack entry's triggering card, from the
 * graveyard, set it as imprinted on the source permanent, and return any previously imprinted card
 * to its owner's graveyard.
 */
public record ImprintDyingCreatureEffect() implements CardEffect {
}
