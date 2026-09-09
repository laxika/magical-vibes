package com.github.laxika.magicalvibes.model.effect;

/**
 * A modal choice made while a triggered ability resolves, after its targets were chosen.
 *
 * <p>The effect may be bound to a target group so an independently optional target remains
 * available to the modal's later options.</p>
 */
public record ChooseOneAtResolutionEffect(ChooseOneEffect choice) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }

    @Override
    public boolean resolvesWhenTargetIllegal() {
        return true;
    }
}
