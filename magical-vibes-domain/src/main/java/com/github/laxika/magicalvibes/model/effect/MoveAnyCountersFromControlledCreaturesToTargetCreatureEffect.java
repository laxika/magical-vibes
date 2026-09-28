package com.github.laxika.magicalvibes.model.effect;

/**
 * Moves any number of counters of any kinds from creatures controlled by the ability controller
 * onto the targeted creature.
 */
public record MoveAnyCountersFromControlledCreaturesToTargetCreatureEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
