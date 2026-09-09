package com.github.laxika.magicalvibes.model.effect;

/**
 * Grants a static effect to target creature until end of turn.
 *
 * @param staticEffect the static effect the target creature gains
 */
public record GrantStaticEffectToTargetUntilEndOfTurnEffect(CardEffect staticEffect)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
