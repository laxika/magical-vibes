package com.github.laxika.magicalvibes.model.effect;

/**
 * When resolved, registers a delayed triggered ability for the rest of the turn:
 * "When this artifact leaves the battlefield this turn, destroy that creature."
 *
 * <p>Reads the shared creature target and the ability's source permanent from the stack entry.
 */
public record RegisterDelayedDestroyTargetWhenSourceLeavesEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
