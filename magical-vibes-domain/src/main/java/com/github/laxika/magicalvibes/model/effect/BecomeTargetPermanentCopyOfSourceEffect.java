package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes the permanent referenced by the stack entry's target id a copy of the source permanent.
 * The target is an implied reference from a triggered event rather than a chosen target.
 */
public record BecomeTargetPermanentCopyOfSourceEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
