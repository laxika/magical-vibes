package com.github.laxika.magicalvibes.model.effect;

/**
 * Double the number of each kind of counter on target permanent (Gilder Bairn).
 * For every counter kind present on the target, adds that many additional counters
 * of the same kind.
 */
public record DoubleCountersOnTargetPermanentEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PERMANENT);
    }
}
