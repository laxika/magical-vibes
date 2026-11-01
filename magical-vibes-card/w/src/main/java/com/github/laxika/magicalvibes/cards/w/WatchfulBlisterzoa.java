package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DrawCardForEachDyingSourceCounterEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONE", collectorNumber = "78")
public class WatchfulBlisterzoa extends Card {

    public WatchfulBlisterzoa() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.OIL, new Fixed(1)));
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                new DrawCardForEachDyingSourceCounterEffect(CounterType.OIL));
    }
}
