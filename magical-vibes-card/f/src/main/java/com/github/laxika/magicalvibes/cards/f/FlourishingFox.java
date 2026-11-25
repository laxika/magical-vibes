package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "IKO", collectorNumber = "13")
public class FlourishingFox extends Card {

    public FlourishingFox() {
        addTrigger(Triggers.youCycleCard(),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
        addCycling("{1}");
    }
}
