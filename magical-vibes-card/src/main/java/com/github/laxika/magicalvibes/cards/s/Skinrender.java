package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SOM", collectorNumber = "78")
@CardRegistration(set = "2X2", collectorNumber = "91")
@CardRegistration(set = "ECC", collectorNumber = "86")
public class Skinrender extends Card {

    public Skinrender() {
        target(TargetFilters.creature()).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 3));
    }
}
