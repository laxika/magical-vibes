package com.github.laxika.magicalvibes.model.effect;

/**
 * Phases the target creature out and keeps it phased out until the source permanent leaves.
 * The source-linked phase-in taps the creature.
 */
public record PhaseOutTargetCreatureUntilSourceLeavesEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
