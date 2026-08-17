package com.github.laxika.magicalvibes.model.effect;

/**
 * Schedule the targeted permanent to be sacrificed at the beginning of the next end step,
 * optionally after a coin flip (Goblin Kites).
 */
public record SacrificeTargetPermanentAtEndStepEffect(boolean flipBeforeSacrificing) implements CardEffect {

    /** Schedules an unconditional sacrifice at the next end step. */
    public SacrificeTargetPermanentAtEndStepEffect() {
        this(false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent());
    }
}
