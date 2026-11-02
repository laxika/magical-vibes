package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FDN", collectorNumber = "236")
@CardRegistration(set = "M21", collectorNumber = "214")
@CardRegistration(set = "CMM", collectorNumber = "332")
public class WildwoodScourge extends Card {

    public WildwoodScourge() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue()));
        addTrigger(Triggers.plusOneCountersPutOnAnotherNonHydraCreatureYouControl(),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
