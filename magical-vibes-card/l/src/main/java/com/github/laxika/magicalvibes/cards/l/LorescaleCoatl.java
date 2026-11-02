package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ARB", collectorNumber = "101")
@CardRegistration(set = "M21", collectorNumber = "221")
@CardRegistration(set = "MM2", collectorNumber = "179")
@CardRegistration(set = "DDO", collectorNumber = "53")
@CardRegistration(set = "A25", collectorNumber = "207")
@CardRegistration(set = "C15", collectorNumber = "225")
public class LorescaleCoatl extends Card {

    public LorescaleCoatl() {
        // Whenever you draw a card, put a +1/+1 counter on this creature.
        addTrigger(Triggers.youDrawCard(), new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
