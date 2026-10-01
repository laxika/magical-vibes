package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect that gives the controller's creature spells offspring with the supplied cost.
 * The cast path materializes the granted ability on the spell's runtime copy.
 */
public record GrantOffspringToCreatureSpellsEffect(String offspringCost) implements CardEffect {
}
