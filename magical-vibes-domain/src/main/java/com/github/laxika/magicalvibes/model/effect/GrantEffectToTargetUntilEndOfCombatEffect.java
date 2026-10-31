package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LegacySlots;

/**
 * When resolved, grants each targeted creature a triggered ability for the rest of the current
 * combat. The grant is removed when combat state is cleared.
 */
public record GrantEffectToTargetUntilEndOfCombatEffect(
        Ability grantedAbility
) implements CardEffect {

    /** The grant as the slot and effect that stand for it. */
    public GrantEffectToTargetUntilEndOfCombatEffect(EffectSlot slot, CardEffect grantedEffect) {
        this(LegacySlots.grant(slot, grantedEffect));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
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
