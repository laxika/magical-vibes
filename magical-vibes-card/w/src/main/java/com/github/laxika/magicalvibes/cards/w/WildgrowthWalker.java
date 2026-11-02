package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "XLN", collectorNumber = "216")
public class WildgrowthWalker extends Card {

    public WildgrowthWalker() {
        // Whenever a creature you control explores, put a +1/+1 counter on
        // Wildgrowth Walker and you gain 3 life.
        addTrigger(Triggers.creatureYouControlExplores(), new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                new GainLifeEffect(3));
    }
}
