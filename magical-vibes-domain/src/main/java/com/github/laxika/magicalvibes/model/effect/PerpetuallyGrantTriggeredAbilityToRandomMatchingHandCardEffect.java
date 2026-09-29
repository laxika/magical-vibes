package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;

/** Perpetually grants triggered effects to one randomly selected matching card in the controller's hand. */
public record PerpetuallyGrantTriggeredAbilityToRandomMatchingHandCardEffect(
        EffectSlot slot, List<CardEffect> grantedEffects, CardPredicate filter) implements CardEffect {

    public PerpetuallyGrantTriggeredAbilityToRandomMatchingHandCardEffect {
        grantedEffects = List.copyOf(grantedEffects);
    }
}
