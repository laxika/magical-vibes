package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH1", collectorNumber = "60")
public class Oneirophage extends Card {

    public Oneirophage() {
        // Whenever you draw a card, put a +1/+1 counter on this creature.
        addTrigger(Triggers.youDrawCard(), new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
