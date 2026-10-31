package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LegacySlots;

/** Registers a global triggered ability until the beginning of the controller's next turn. */
public record RegisterGlobalTriggeredAbilityUntilNextTurnEffect(Ability grantedAbility)
        implements CardEffect {

    /** The grant as the slot and effect that stand for it. */
    public RegisterGlobalTriggeredAbilityUntilNextTurnEffect(EffectSlot slot, CardEffect triggeredEffect) {
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
