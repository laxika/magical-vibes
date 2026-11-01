package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSH", collectorNumber = "66")
public class MisterFantasticReedRichards extends Card {

    public MisterFantasticReedRichards() {
        addTrigger(new TriggerCondition(Triggers.enters(Triggers.TOKEN, ObjectRelation.YOU_CONTROL)),
                TriggerOptions.ONE_OR_MORE,
                new MayEffect(new DrawCardEffect(), "Draw a card?"));
    }
}
