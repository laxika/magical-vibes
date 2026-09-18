package com.github.laxika.magicalvibes.model.effect;

/**
 * Removes a creature defending player controls from combat, unblocks attackers that were
 * blocked only by it this combat, and optionally has it block a chosen attacking creature.
 */
public record FalseOrdersEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
