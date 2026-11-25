package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.TriggeringPermanentHasGreaterPowerOrToughnessThanSource;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSH", collectorNumber = "173")
public class HulklingBurgeoningBruiser extends Card {

    public HulklingBurgeoningBruiser() {
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new TriggeringPermanentHasGreaterPowerOrToughnessThanSource()),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
