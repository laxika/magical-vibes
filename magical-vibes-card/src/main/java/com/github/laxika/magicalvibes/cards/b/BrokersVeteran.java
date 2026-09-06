package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SNC", collectorNumber = "36")
public class BrokersVeteran extends Card {

    public BrokersVeteran() {
        target(TargetFilters.creatureYouControl()).addEffect(EffectSlot.ON_DEATH,
                new PutCounterOnTargetPermanentEffect(CounterType.SHIELD, 1));
    }
}
