package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: "{this} can't be chosen." While the source is on the battlefield with its
 * abilities, it is excluded from every choice of an object, such as "each player chooses a
 * creature", "choose a permanent to sacrifice" and "choose target creature" (Glimpse, the
 * Unthinkable).
 */
public record CantBeChosenEffect() implements CardEffect {
}
