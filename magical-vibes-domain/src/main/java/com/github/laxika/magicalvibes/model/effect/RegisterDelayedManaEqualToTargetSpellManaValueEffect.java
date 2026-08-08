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
 * <p>Plasm Capture uses the same shape with {@code optional = false},
 * {@code anyColorCombination = true} and {@code firstMainOnly = true}: "At the beginning of your
 * next first main phase, add X mana in any combination of colors, where X is that spell's mana
 * value."
 *
 * @param color              the color of mana the delayed trigger adds ({C} for Scattering Stroke);
 *                           ignored when {@code anyColorCombination} is true
 * @param optional           whether the delayed ability reads "you may add"
 * @param anyColorCombination whether the mana is added "in any combination of colors" (a color
 *                           chosen per mana) instead of a fixed color
 * @param firstMainOnly      whether the trigger waits for a precombat ("first") main phase
 */
public record RegisterDelayedManaEqualToTargetSpellManaValueEffect(ManaColor color,
                                                                   boolean optional,
                                                                   boolean anyColorCombination,
                                                                   boolean firstMainOnly) implements CardEffect {

    /** Scattering Stroke: "you may add an amount of {C}" at either main phase. */
    public RegisterDelayedManaEqualToTargetSpellManaValueEffect(ManaColor color) {
        this(color, true, false, false);
    }
}
