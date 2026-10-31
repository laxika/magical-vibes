package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LegacySlots;

/** Perpetually grants a triggered ability to the creatures currently controlled by the source. */
public record PerpetuallyGrantTriggeredAbilityToOwnCreaturesEffect(
        Ability grantedAbility) implements CardEffect {

    /** The grant as the slot and effect that stand for it. */
    public PerpetuallyGrantTriggeredAbilityToOwnCreaturesEffect(EffectSlot triggeredAbilitySlot,
                                                                CardEffect triggeredAbility) {
        this(LegacySlots.grant(triggeredAbilitySlot, triggeredAbility));
    }

    /** The slot of {@link #grantedAbility}, or {@code null} when there is none. */
    public EffectSlot triggeredAbilitySlot() {
        return grantedAbility == null ? null : LegacySlots.slotOf(grantedAbility);
    }

    /** The effect of {@link #grantedAbility}, or {@code null} when there is none. */
    public CardEffect triggeredAbility() {
        return grantedAbility == null ? null : LegacySlots.effectsOf(grantedAbility).getFirst();
    }
}
