package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;

/** Lets the controller choose a card in the target player's hand to receive persistent triggers. */
public record PerpetuallyGrantTriggeredAbilityToTargetCardInHandEffect(
        EffectSlot slot, List<CardEffect> grantedEffects, CardPredicate filter) implements CardEffect {

    public PerpetuallyGrantTriggeredAbilityToTargetCardInHandEffect {
        grantedEffects = List.copyOf(grantedEffects);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
