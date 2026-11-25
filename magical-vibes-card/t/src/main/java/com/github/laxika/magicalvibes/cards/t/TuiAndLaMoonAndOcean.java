package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TLE", collectorNumber = "96")
@CardRegistration(set = "TLE", collectorNumber = "181")
public class TuiAndLaMoonAndOcean extends Card {

    public TuiAndLaMoonAndOcean() {
        addTrigger(Triggers.becomesTapped(), new DrawCardEffect(1));
        addTrigger(Triggers.becomesUntapped(), PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
