package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;

/** Perpetually grants triggered effects to every matching card currently in the controller's hand. */
public record PerpetuallyGrantTriggeredAbilityToMatchingHandCardsEffect(
        EffectSlot slot, List<CardEffect> grantedEffects, CardPredicate filter) implements CardEffect {

    public PerpetuallyGrantTriggeredAbilityToMatchingHandCardsEffect {
        grantedEffects = List.copyOf(grantedEffects);
    }
}
