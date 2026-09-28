package com.github.laxika.magicalvibes.model.effect;

/**
 * Causes the source permanent to become a copy of the target creature for as long as that target
 * remains tapped. The floating copy effect is keyed to the target permanent, not the source.
 */
public record BecomeCopyOfTargetCreatureWhileTargetTappedEffect() implements TemporaryCopyEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
