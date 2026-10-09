package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscoverEffect;
import com.github.laxika.magicalvibes.model.effect.TargetDealsPowerDamageToTargetEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "LCC", collectorNumber = "12")
@CardRegistration(set = "LCC", collectorNumber = "24")
public class ContestOfClaws extends Card {

    public ContestOfClaws() {
        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "First target must be a creature you control"
        ));

        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, TargetDealsPowerDamageToTargetEffect.recordingExcessDamage())
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(
                        new EventValueAtLeast(1),
                        new DiscoverEffect(new EventValue())));
    }
}
