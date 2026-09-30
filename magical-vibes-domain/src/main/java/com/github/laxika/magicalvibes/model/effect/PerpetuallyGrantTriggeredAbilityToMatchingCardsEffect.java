package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Perpetually grants a triggered ability to matching permanents and cards in hand. */
public record PerpetuallyGrantTriggeredAbilityToMatchingCardsEffect(
        CardPredicate handFilter,
        PermanentPredicate permanentFilter,
        EffectSlot triggeredAbilitySlot,
        CardEffect triggeredAbility) implements CardEffect {

    public PerpetuallyGrantTriggeredAbilityToMatchingCardsEffect {
        if (triggeredAbilitySlot == null || triggeredAbility == null) {
            throw new IllegalArgumentException("Triggered ability slot and effect are required");
        }
    }
}
