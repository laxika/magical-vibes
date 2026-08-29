package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability for a sacrifice trigger that needs the sacrificed permanent's mana value.
 * The sacrifice trigger collector binds the last-known value before the effect is put on the stack.
 */
public interface SacrificedPermanentManaValueAwareEffect {

    /** Returns the effect with the sacrificed permanent's mana value bound in. */
    CardEffect boundToSacrificedPermanentManaValue(int manaValue);
}
