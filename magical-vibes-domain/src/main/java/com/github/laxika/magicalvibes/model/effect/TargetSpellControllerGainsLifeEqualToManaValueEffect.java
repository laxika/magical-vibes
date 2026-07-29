package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes the controller of the targeted spell gain life equal to that spell's mana value.
 * Used as a companion effect alongside counter-spell effects (e.g. Illumination); place it before
 * the counter effect so the targeted spell is still on the stack when this resolves.
 * Does not independently target — piggybacks on the spell's existing target.
 */
public record TargetSpellControllerGainsLifeEqualToManaValueEffect() implements CardEffect {
}
