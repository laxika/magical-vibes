package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Conditional wrapper that only applies while the source permanent has at least
 * {@code threshold} counters of the given type (e.g. "as long as there are five or more
 * growth counters on this enchantment").
 */
public record SourceCounterThresholdConditionalEffect(
        int threshold,
        CounterType counterType,
        CardEffect wrapped
) implements ConditionalEffect {

    @Override
    public String conditionName() {
        return counterType.name().toLowerCase() + " counter threshold (" + threshold + "+)";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + threshold + " " + counterType.name().toLowerCase() + " counters on source";
    }

    @Override
    public boolean canTargetPlayer() {
        return wrapped.canTargetPlayer();
    }

    @Override
    public boolean canTargetPermanent() {
        return wrapped.canTargetPermanent();
    }

    @Override
    public boolean canTargetSpell() {
        return wrapped.canTargetSpell();
    }

    @Override
    public boolean canTargetGraveyard() {
        return wrapped.canTargetGraveyard();
    }
}
