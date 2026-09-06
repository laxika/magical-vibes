package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "4ED", collectorNumber = "163")
@CardRegistration(set = "LEG", collectorNumber = "117")
public class SpiritShackle extends Card {

    public SpiritShackle() {
        target(TargetFilters.creature());
        // Whenever enchanted creature becomes tapped, put a -0/-2 counter on it.
        addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_TAPPED,
                new PutCounterOnReferencedPermanentEffect(CounterType.MINUS_ZERO_MINUS_TWO));
    }
}
