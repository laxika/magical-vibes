package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MB2", collectorNumber = "302")
@CardRegistration(set = "MB2", collectorNumber = "538")
public class TwoByFour extends Card {

    public TwoByFour() {
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Put a base power 4 counter on target creature",
                        PutCounterOnTargetPermanentEffect.withTargetRestriction(
                                CounterType.BASE_POWER_FOUR, 1, new PermanentIsCreaturePredicate()),
                        TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption(
                        "Put a base toughness 4 counter on target creature",
                        PutCounterOnTargetPermanentEffect.withTargetRestriction(
                                CounterType.BASE_TOUGHNESS_FOUR, 1, new PermanentIsCreaturePredicate()),
                        TargetFilters.creature())
        )));
    }
}
