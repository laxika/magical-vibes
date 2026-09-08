package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;

/**
 * When resolved, grants the source permanent a triggered ability for the rest of the current
 * combat. The grant is removed when combat state is cleared, before a later combat can begin.
 */
public record GrantEffectToSourceUntilEndOfCombatEffect(
        EffectSlot slot,
        CardEffect grantedEffect
) implements CardEffect {
}
