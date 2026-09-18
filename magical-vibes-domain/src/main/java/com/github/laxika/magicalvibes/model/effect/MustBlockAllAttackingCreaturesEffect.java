package com.github.laxika.magicalvibes.model.effect;

/**
 * Requires the targeted creature to block every attacking creature it can legally block this turn.
 * The attacking creatures are captured when the effect resolves and enforced through the shared
 * per-creature blocking-requirement path.
 */
public record MustBlockAllAttackingCreaturesEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
