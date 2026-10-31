package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LegacySlots;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

/** Registers a global triggered ability through the end of the controller's next turn. */
public record RegisterGlobalTriggeredAbilityUntilEndOfNextTurnEffect(Ability grantedAbility,
                                                                      TargetFilter targetFilter)
        implements CardEffect {

    /** The grant as the slot and effect that stand for it. */
    public RegisterGlobalTriggeredAbilityUntilEndOfNextTurnEffect(EffectSlot slot,
                                                                  CardEffect triggeredEffect,
                                                                  TargetFilter targetFilter) {
        this(LegacySlots.grant(slot, triggeredEffect), targetFilter);
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
