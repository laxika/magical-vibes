package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * When resolved, grants each matching creature the controller currently controls a triggered
 * ability for the rest of the current combat. The optional filter is evaluated at resolution.
 */
public record GrantEffectToOwnCreaturesUntilEndOfCombatEffect(
        EffectSlot slot,
        CardEffect grantedEffect,
        PermanentPredicate filter
) implements CardEffect {

    public GrantEffectToOwnCreaturesUntilEndOfCombatEffect(EffectSlot slot, CardEffect grantedEffect) {
        this(slot, grantedEffect, null);
    }
}
