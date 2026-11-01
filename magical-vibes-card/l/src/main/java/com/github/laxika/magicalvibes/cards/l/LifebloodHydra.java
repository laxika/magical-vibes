package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C14", collectorNumber = "45")
@CardRegistration(set = "CMM", collectorNumber = "303")
@CardRegistration(set = "CMM", collectorNumber = "565")
@CardRegistration(set = "CMA", collectorNumber = "124")
@CardRegistration(set = "SOC", collectorNumber = "274")
public class LifebloodHydra extends Card {

    public LifebloodHydra() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue()));
        addTrigger(Triggers.dies(ObjectRelation.THIS), SequenceEffect.of(
                new GainLifeEffect(new EventValue()),
                new DrawCardEffect(new EventValue())));
    }
}
