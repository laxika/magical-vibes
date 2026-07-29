package com.github.laxika.magicalvibes.model.effect;

/**
 * Rider for {@link DestroyAllPermanentsEffect}: "The controller of each of those permanents gains
 * life equal to its mana value."
 *
 * <p>One life gain per destroyed permanent, not per player — a player who lost three artifacts gains
 * the sum of their three mana values. The recipients come from the {@code StackEntry.eventPlayerIds}
 * channel the destroy-all handler stamps with the controller of every permanent actually destroyed,
 * paired positionally with the last-known mana values on {@code StackEntry.eventManaValues}
 * (indestructible / regenerated permanents are absent from both, so they grant no life).
 * Seeds of Innocence.
 *
 * <p>Deliberately not a {@link LifeGainEffect}: the life goes to the destroyed permanents'
 * controllers (usually opponents), not to a single amount for this effect's controller.
 */
public record EachDestroyedPermanentControllerGainsLifeEqualToManaValueEffect() implements CardEffect {
}
