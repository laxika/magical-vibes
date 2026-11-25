package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.TriggeringPermanentPower;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseOpponentLifeCostForTargetingControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M21", collectorNumber = "164")
@CardRegistration(set = "M21", collectorNumber = "369")
@CardRegistration(set = "OTJ", collectorNumber = "149")
public class TerrorOfThePeaks extends Card {

    public TerrorOfThePeaks() {
        addStaticEffect(new IncreaseOpponentLifeCostForTargetingControlledPermanentEffect(
                new PermanentIsSourcePermanentPredicate(), 3));
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                DealDamageEffect.toAnyTarget(new TriggeringPermanentPower()));
    }
}
