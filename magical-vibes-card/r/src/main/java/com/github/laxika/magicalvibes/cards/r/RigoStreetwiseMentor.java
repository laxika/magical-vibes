package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.HasAttacker;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SNC", collectorNumber = "215")
public class RigoStreetwiseMentor extends Card {

    public RigoStreetwiseMentor() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.SHIELD, new Fixed(1)));
        addTrigger(Triggers.youAttackPlayerOrPlaneswalker(),
                TriggerOptions.DEFAULT.withEventCondition(new HasAttacker(new PermanentPowerAtMostPredicate(1))),
                new DrawCardEffect(1));
    }
}
