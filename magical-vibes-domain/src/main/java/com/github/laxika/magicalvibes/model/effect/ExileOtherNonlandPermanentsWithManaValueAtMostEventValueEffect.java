package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles each other nonland permanent with mana value less than or equal to the resolving
 * stack entry's event value. The effect is intended to follow an effect that records a removed
 * counter count as its event value, such as Sarulf, Realm Eater's upkeep ability.
 */
public record ExileOtherNonlandPermanentsWithManaValueAtMostEventValueEffect() implements CardEffect {
}
