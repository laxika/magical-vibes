package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DoubleCountersOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M21", collectorNumber = "190")
public class InvigoratingSurge extends Card {

    public InvigoratingSurge() {
        target(TargetFilters.creatureYouControl())
                .addSpellEffect(new PutCountersEffect(CounterType.PLUS_ONE_PLUS_ONE))
                .addSpellEffect(new DoubleCountersOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
