package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LegacySlots;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Perpetually grants a triggered ability to matching permanents and cards in hand. */
public record PerpetuallyGrantTriggeredAbilityToMatchingCardsEffect(
        CardPredicate handFilter,
        PermanentPredicate permanentFilter,
        Ability grantedAbility) implements CardEffect {

    /** The grant as the slot and effect that stand for it. */
    public PerpetuallyGrantTriggeredAbilityToMatchingCardsEffect(CardPredicate handFilter,
                                                                 PermanentPredicate permanentFilter,
                                                                 EffectSlot triggeredAbilitySlot,
                                                                 CardEffect triggeredAbility) {
        if (triggeredAbilitySlot == null || triggeredAbility == null) {
            throw new IllegalArgumentException("Triggered ability slot and effect are required");
        }
        this(handFilter, permanentFilter, LegacySlots.grant(triggeredAbilitySlot, triggeredAbility));
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
