package com.github.laxika.magicalvibes.model.effect;

/** Goads the chosen target creature until the effect controller's next turn. */
public record GoadTargetCreatureUntilNextTurnEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
