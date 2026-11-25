package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BRO", collectorNumber = "56")
public class LatNamAdept extends Card {

    public LatNamAdept() {
        addTrigger(Triggers.youDrawSecondCardEachTurn(), PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
