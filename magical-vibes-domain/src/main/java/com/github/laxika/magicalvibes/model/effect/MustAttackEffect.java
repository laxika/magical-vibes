package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: this creature attacks each combat if able.
 * Per CR 508.1d, the controller is not required to pay any attack costs
 * (e.g. Ghostly Prison tax) even if this effect is present.
 */
public record MustAttackEffect() implements CardEffect {
}
