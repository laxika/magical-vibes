package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EOE", collectorNumber = "197")
public class MeltstriderEulogist extends Card {

    public MeltstriderEulogist() {
        addTrigger(Triggers.creatureDies(ObjectRelation.YOU_CONTROL),
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE),
                        new DrawCardEffect(1)));
    }
}
