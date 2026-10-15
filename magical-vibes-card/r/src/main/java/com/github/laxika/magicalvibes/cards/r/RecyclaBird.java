package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MB1", collectorNumber = "28")
public class RecyclaBird extends Card {

    public RecyclaBird() {
        target(TargetFilters.creatureYouControl()).addEffect(
                EffectSlot.ON_DEATH,
                new PutCounterOnTargetPermanentEffect(CounterType.FLYING));
    }
}
