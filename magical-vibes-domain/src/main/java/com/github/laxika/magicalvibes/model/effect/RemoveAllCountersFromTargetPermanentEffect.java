package com.github.laxika.magicalvibes.model.effect;

/**
 * "Remove all counters from target permanent."
 *
 * <p>Removes every counter of every concrete type from the target permanent and records the number
 * removed as the stack entry's event value for a following {@code EventValue} effect. This effect
 * does not prevent the permanent from receiving counters again after resolution.</p>
 */
public record RemoveAllCountersFromTargetPermanentEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
