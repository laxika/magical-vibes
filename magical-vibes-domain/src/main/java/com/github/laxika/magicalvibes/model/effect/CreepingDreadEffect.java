package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes every player discard one card, then makes each opponent who discarded a card sharing a
 * card type with the controller's discarded card lose 3 life.
 */
public record CreepingDreadEffect() implements CardEffect {
}
