package com.github.laxika.magicalvibes.model.effect;

/**
 * Schedules the targeted creature for destruction at the beginning of the next end step if it
 * attacked this turn.
 *
 * <p>The attack check is retained on the delayed ability and is therefore evaluated at the end
 * step, rather than when this effect resolves.</p>
 */
public record DestroyTargetPermanentAtEndStepIfAttackedEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
