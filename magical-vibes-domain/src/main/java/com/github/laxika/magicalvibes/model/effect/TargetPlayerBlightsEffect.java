package com.github.laxika.magicalvibes.model.effect;

/**
 * Has the target player blight the specified number of times.
 */
public record TargetPlayerBlightsEffect(int count) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
