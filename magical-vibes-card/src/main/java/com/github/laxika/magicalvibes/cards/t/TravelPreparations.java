package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ISD", collectorNumber = "206")
@CardRegistration(set = "INR", collectorNumber = "220")
@CardRegistration(set = "INR", collectorNumber = "421")
public class TravelPreparations extends Card {

    public TravelPreparations() {
        target(TargetFilters.creature(), 1, 2)
                .addEffect(EffectSlot.SPELL, new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));
        addCastingOption(new FlashbackCast("{1}{W}"));
    }
}
