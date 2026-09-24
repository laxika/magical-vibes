package com.github.laxika.magicalvibes.model.effect;

/**
 * Exchanges the source creature's power with the power of the target creature until end of
 * combat. Both powers are locked in when the effect resolves; other power modifiers continue to
 * apply to the exchanged values.
 */
public record ExchangePowerWithTargetUntilEndOfCombatEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
