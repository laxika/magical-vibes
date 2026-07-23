package com.github.laxika.magicalvibes.model.effect;

/**
 * "This permanent becomes the color or colors of your choice" with no stated duration (Shyft).
 * Self-scoped: no target. On resolution the controller picks one or more colors; the choice
 * handler then floats a layer-5 {@link BecomeChosenColorsUntilEndOfTurnEffect} carrying those
 * colors with {@link EffectDuration#PERMANENT} (CR 105.3 / 611.2b — lasts until another effect
 * changes the colors or the permanent leaves).
 */
public record BecomeChosenColorsIndefinitelyEffect() implements CardEffect {
}
