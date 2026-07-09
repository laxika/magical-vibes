package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * Scattering Stroke's clash-win reward. Used as the wrapped effect of a {@link ClashEffect} on a
 * "counter target spell" card: when it resolves it reads the targeted spell's mana value (while the
 * spell is still on the stack — hence the clash/reward must resolve before the counter removes it)
 * and registers a delayed trigger that, at the beginning of the controller's next main phase, lets
 * them add that much mana of {@code color} ("you may add an amount of {C} equal to that spell's mana
 * value").
 *
 * @param color the color of mana the delayed trigger may add ({C} for Scattering Stroke)
 */
public record RegisterDelayedManaEqualToTargetSpellManaValueEffect(ManaColor color) implements CardEffect {
}
