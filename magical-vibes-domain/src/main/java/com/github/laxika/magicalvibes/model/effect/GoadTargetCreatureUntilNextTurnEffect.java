package com.github.laxika.magicalvibes.model.effect;

/**
 * Goads one targeted creature until the ability controller's next turn.
 */
public record GoadTargetCreatureUntilNextTurnEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
