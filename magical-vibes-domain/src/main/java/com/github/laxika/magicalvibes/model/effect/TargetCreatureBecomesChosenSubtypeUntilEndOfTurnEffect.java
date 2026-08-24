package com.github.laxika.magicalvibes.model.effect;

/**
 * One-shot effect: the targeted creature becomes the creature type chosen at resolution until end
 * of turn, replacing all of its other creature types.
 */
public record TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
