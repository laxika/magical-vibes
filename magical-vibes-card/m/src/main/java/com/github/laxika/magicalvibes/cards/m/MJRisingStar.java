package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SPE", collectorNumber = "3")
@CardRegistration(set = "MSC", collectorNumber = "773")
public class MJRisingStar extends Card {

    public MJRisingStar() {
        // Whenever you gain life, put a +1/+1 counter on MJ.
        addTrigger(Triggers.youGainLife(), PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
