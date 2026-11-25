package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerHasCityBlessing;
import com.github.laxika.magicalvibes.model.effect.AscendEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RIX", collectorNumber = "78")
public class MausoleumHarpy extends Card {

    public MausoleumHarpy() {
        addStaticEffect(new AscendEffect());
        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER_YOU_CONTROL),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerHasCityBlessing()),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
