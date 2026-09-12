package com.github.laxika.magicalvibes.model.effect;

/** Prevents damage from face-up planes with the given name to the resolving player's permanents for the game. */
public record PreventDamageFromNamedPlanesToControlledPermanentsEffect(String planeName) implements CardEffect {
}
