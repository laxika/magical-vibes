package com.github.laxika.magicalvibes.model.effect;

/**
 * "Becomes colorless" with no stated duration (CR 611.2b), i.e. the layer-5 color-setting effect
 * lasts as long as the affected permanent exists. The indefinite sibling of
 * {@link BecomeColorlessUntilEndOfTurnEffect}: the handler floats that record with an
 * {@link EffectDuration#PERMANENT} duration, exactly as
 * {@link BecomeChosenColorsIndefinitelyEffect} does for a chosen color set.
 *
 * <p>With {@code targeted = false} the effect is self-scoped; with {@code targeted = true} it
 * applies to the chosen permanent (Xathrid Gorgon's petrification ability).
 */
public record BecomeColorlessIndefinitelyEffect(boolean targeted) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return targeted ? TargetSpec.benign(TargetCategory.PERMANENT) : TargetSpec.NONE;
    }
}
