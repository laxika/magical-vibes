package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseOpponentLifeCostForTargetingControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.amount.SourcePower;

@CardRegistration(set = "M21", collectorNumber = "164")
@CardRegistration(set = "M21", collectorNumber = "369")
@CardRegistration(set = "OTJ", collectorNumber = "149")
public class TerrorOfThePeaks extends Card {

    public TerrorOfThePeaks() {
        addStaticEffect(new IncreaseOpponentLifeCostForTargetingControlledPermanentEffect(
                new PermanentIsSourcePermanentPredicate(), 3));
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                DealDamageToAnyTargetEffect.dealtByTriggeringPermanent(new SourcePower()));
    }
}
