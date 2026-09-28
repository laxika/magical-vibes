package com.github.laxika.magicalvibes.model.effect;

/** Exiles target creature and registers its card for suspend with time counters. */
public record ExileTargetCreatureWithSuspendEffect(int timeCounters) implements RemovalEffect {

    public ExileTargetCreatureWithSuspendEffect {
        if (timeCounters < 1) {
            throw new IllegalArgumentException("timeCounters must be positive");
        }
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.EXILE;
    }
}
