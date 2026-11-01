package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WOE", collectorNumber = "28")
public class SaviorOfTheSleeping extends Card {

    public SaviorOfTheSleeping() {
        addTrigger(Triggers.dies(Triggers.ENCHANTMENT, ObjectRelation.YOU_CONTROL),
                new PutCountersOnSourceEffect(1, 1, 1));
    }
}
