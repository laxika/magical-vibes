package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "GRN", collectorNumber = "134")
public class IronshellBeetle extends Card {

    public IronshellBeetle() {
        // "When this creature enters, put a +1/+1 counter on target creature."
        target(TargetFilters.creature()).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));
    }
}
