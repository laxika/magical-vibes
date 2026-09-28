package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;

/**
 * When resolved, grants each targeted creature a triggered ability for the rest of the current
 * combat. The grant is removed when combat state is cleared.
 */
public record GrantEffectToTargetUntilEndOfCombatEffect(
        EffectSlot slot,
        CardEffect grantedEffect
) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
