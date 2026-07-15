package com.github.laxika.magicalvibes.model.effect;

/**
 * "Remove a counter from target permanent." Removes a single counter of any one kind currently on
 * the target. If the target has counters of several kinds (a rare state), one of the first present
 * kind is removed. No-op when the target has no counters. Used by Medicine Runner.
 */
public record RemoveCounterFromTargetPermanentEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PERMANENT);
    }
}
