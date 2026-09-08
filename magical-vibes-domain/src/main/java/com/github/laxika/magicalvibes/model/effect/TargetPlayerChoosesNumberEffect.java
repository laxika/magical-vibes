package com.github.laxika.magicalvibes.model.effect;

/**
 * The target player chooses a nonnegative integer during resolution and stores it as the
 * resolving stack entry's event value for a following effect.
 */
public record TargetPlayerChoosesNumberEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
