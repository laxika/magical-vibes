package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WOE", collectorNumber = "28")
public class SaviorOfTheSleeping extends Card {

    public SaviorOfTheSleeping() {
        addTrigger(Triggers.dies(Triggers.ENCHANTMENT, ObjectRelation.YOU_CONTROL),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
