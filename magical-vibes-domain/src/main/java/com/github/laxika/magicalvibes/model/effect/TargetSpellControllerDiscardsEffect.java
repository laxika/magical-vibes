package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes the controller of the targeted spell discard cards.
 * Used as a companion effect alongside counter-spell effects (e.g. Frightful Delusion).
 * Does not independently target — piggybacks on the spell's existing target.
 * Place before the counter effect so the target is still on the stack.
 */
public record TargetSpellControllerDiscardsEffect(int amount) implements CardEffect {
}
