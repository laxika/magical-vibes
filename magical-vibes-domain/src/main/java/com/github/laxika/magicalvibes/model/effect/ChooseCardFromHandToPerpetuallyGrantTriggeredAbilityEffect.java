package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;
import java.util.Objects;

/** Chooses a matching card in the controller's hand and perpetually grants it a triggered ability. */
public record ChooseCardFromHandToPerpetuallyGrantTriggeredAbilityEffect(
        EffectSlot slot, List<CardEffect> grantedEffects, CardPredicate cardFilter) implements CardEffect {

    public ChooseCardFromHandToPerpetuallyGrantTriggeredAbilityEffect {
        Objects.requireNonNull(slot, "slot");
        grantedEffects = List.copyOf(grantedEffects);
        Objects.requireNonNull(cardFilter, "cardFilter");
    }
}
