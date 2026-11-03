package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.HasAttacker;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SNC", collectorNumber = "203")
public class MetropolisAngel extends Card {

    public MetropolisAngel() {
        addTrigger(Triggers.youAttack(),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new HasAttacker(new PermanentHasCountersPredicate(CounterType.ANY))),
                new DrawCardEffect());
    }
}
