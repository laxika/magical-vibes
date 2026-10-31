package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LegacySlots;

/**
 * Grants the source permanent a temporary triggered ability whose event subject must match the
 * color chosen earlier during the same resolution.
 */
public record GrantEffectToSourceOfChosenColorUntilEndOfTurnEffect(
        Ability grantedAbility
) implements CardEffect {

    /** The grant as the slot and effect that stand for it. */
    public GrantEffectToSourceOfChosenColorUntilEndOfTurnEffect(EffectSlot slot, CardEffect grantedEffect) {
        this(LegacySlots.grant(slot, grantedEffect));
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
