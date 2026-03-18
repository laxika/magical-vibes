package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnterWithXPlusOnePlusOneCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "23")
public class MikaeusTheLunarch extends Card {

    public MikaeusTheLunarch() {
        // Mikaeus enters with X +1/+1 counters on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithXPlusOnePlusOneCountersEffect());

        // {T}: Put a +1/+1 counter on Mikaeus.
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new PutCounterOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                "{T}: Put a +1/+1 counter on Mikaeus, the Lunarch."));

        // {T}, Remove a +1/+1 counter from Mikaeus: Put a +1/+1 counter on each other creature you control.
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.PLUS_ONE_PLUS_ONE),
                        new PutPlusOnePlusOneCounterOnEachControlledPermanentEffect(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                                ))
                        )
                ),
                "{T}, Remove a +1/+1 counter from Mikaeus, the Lunarch: Put a +1/+1 counter on each other creature you control."));
    }
}
