package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LRW", collectorNumber = "237")
public class SylvanEchoes extends Card {

    public SylvanEchoes() {
        addTrigger(Triggers.youClash(), TriggerOptions.DEFAULT.withEventCondition(new EventValueAtLeast(1)),
                new MayEffect(new DrawCardEffect(1), "Draw a card?"));
    }
}
