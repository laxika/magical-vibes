package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "KLD", collectorNumber = "159")
public class KujarSeedsculptor extends Card {

    public KujarSeedsculptor() {
        // "When this creature enters, put a +1/+1 counter on target creature you control."
        target(TargetFilters.creatureYouControl()).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));
    }
}
