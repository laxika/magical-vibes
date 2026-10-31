package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LegacySlots;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * When resolved, grants each matching creature the controller currently controls a triggered
 * ability for the rest of the current combat. The optional filter is evaluated at resolution.
 */
public record GrantEffectToOwnCreaturesUntilEndOfCombatEffect(
        Ability grantedAbility,
        PermanentPredicate filter
) implements CardEffect {

    /** The grant as the slot and effect that stand for it. */
    public GrantEffectToOwnCreaturesUntilEndOfCombatEffect(EffectSlot slot,
                                                           CardEffect grantedEffect,
                                                           PermanentPredicate filter) {
        this(LegacySlots.grant(slot, grantedEffect), filter);
    }

    public GrantEffectToOwnCreaturesUntilEndOfCombatEffect(EffectSlot slot, CardEffect grantedEffect) {
        this(slot, grantedEffect, null);
    }

    /** The slot of {@link #grantedAbility}, or {@code null} when there is none. */
    public EffectSlot slot() {
        return grantedAbility == null ? null : LegacySlots.slotOf(grantedAbility);
    }

    /** The effect of {@link #grantedAbility}, or {@code null} when there is none. */
    public CardEffect grantedEffect() {
        return grantedAbility == null ? null : LegacySlots.effectsOf(grantedAbility).getFirst();
    }
}
