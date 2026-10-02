package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;
import java.util.Objects;

/** Lets the controller choose up to a bounded number of matching hand cards for a perpetual trigger grant. */
public record ChooseCardsFromHandToPerpetuallyGrantTriggeredAbilityEffect(
        int maxCount, EffectSlot slot, List<CardEffect> grantedEffects, CardPredicate cardFilter)
        implements CardEffect {

    public ChooseCardsFromHandToPerpetuallyGrantTriggeredAbilityEffect {
        if (maxCount < 1) {
            throw new IllegalArgumentException("maxCount must be positive");
        }
        Objects.requireNonNull(slot, "slot");
        grantedEffects = List.copyOf(grantedEffects);
        Objects.requireNonNull(cardFilter, "cardFilter");
    }
}
