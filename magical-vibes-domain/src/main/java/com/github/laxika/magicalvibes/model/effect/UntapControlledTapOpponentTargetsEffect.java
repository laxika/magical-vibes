package com.github.laxika.magicalvibes.model.effect;

/**
 * For each target, untaps it when the effect controller controls it and taps it otherwise.
 */
public record UntapControlledTapOpponentTargetsEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
