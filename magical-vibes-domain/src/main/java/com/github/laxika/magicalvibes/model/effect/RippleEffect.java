package com.github.laxika.magicalvibes.model.effect;

/**
 * Trigger descriptor for Ripple: reveal the top cards of the controller's library and offer any
 * revealed cards with the same name as the spell for free casts.
 */
public record RippleEffect(int count) implements CardEffect {
}
