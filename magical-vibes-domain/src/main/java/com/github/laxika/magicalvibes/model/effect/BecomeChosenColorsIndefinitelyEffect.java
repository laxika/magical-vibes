package com.github.laxika.magicalvibes.model.effect;

/**
 * "Becomes the color or colors of your choice" with no stated duration. On resolution the
 * controller picks one or more colors; the choice handler then floats a layer-5
 * {@link BecomeChosenColorsUntilEndOfTurnEffect} carrying those colors with
 * {@link EffectDuration#PERMANENT} (CR 105.3 / 611.2b — lasts until another effect changes the
 * colors or the permanent leaves).
 *
 * @param targeted {@code false} (the no-arg form) scopes the effect to the source permanent —
 *                 Shyft's "this creature becomes…". {@code true} makes it a permanent-targeting
 *                 spell — Prismatic Lace's "target permanent becomes…".
 */
public record BecomeChosenColorsIndefinitelyEffect(boolean targeted) implements CardEffect {

    public BecomeChosenColorsIndefinitelyEffect() {
        this(false);
    }

    @Override
    public TargetSpec targetSpec() {
        return targeted ? TargetSpec.benign(TargetCategory.PERMANENT) : TargetSpec.NONE;
    }
}
