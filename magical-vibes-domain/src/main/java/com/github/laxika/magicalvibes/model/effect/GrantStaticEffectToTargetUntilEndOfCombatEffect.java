package com.github.laxika.magicalvibes.model.effect;

/**
 * Grants a static effect to target creature until end of combat.
 *
 * @param staticEffect the static effect the target creature gains
 */
public record GrantStaticEffectToTargetUntilEndOfCombatEffect(CardEffect staticEffect)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
