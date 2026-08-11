package com.github.laxika.magicalvibes.model.effect;

/**
 * "Remove all counters from target permanent."
 *
 * <p>Removes every counter of every concrete type from the target permanent. This effect does not
 * prevent the permanent from receiving counters again after resolution.</p>
 */
public record RemoveAllCountersFromTargetPermanentEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
