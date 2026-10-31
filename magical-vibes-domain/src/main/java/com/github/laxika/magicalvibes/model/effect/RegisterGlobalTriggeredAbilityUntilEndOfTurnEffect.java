package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LegacySlots;

/**
 * Registers a global triggered ability until end of turn. This is used for effects that watch an
 * event across all players' permanents and therefore cannot be stored on one permanent.
 */
public record RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect(Ability grantedAbility)
        implements CardEffect {

    /** The grant as the slot and effect that stand for it. */
    public RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect(EffectSlot slot, CardEffect triggeredEffect) {
        this(LegacySlots.grant(slot, triggeredEffect));
    }

    /** The slot of {@link #grantedAbility}, or {@code null} when there is none. */
    public EffectSlot slot() {
        return grantedAbility == null ? null : LegacySlots.slotOf(grantedAbility);
    }

    /** The effect of {@link #grantedAbility}, or {@code null} when there is none. */
    public CardEffect triggeredEffect() {
        return grantedAbility == null ? null : LegacySlots.effectsOf(grantedAbility).getFirst();
    }
}
