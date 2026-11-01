package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MID", collectorNumber = "29")
@CardRegistration(set = "DBL", collectorNumber = "29")
public class OdricsOutrider extends Card {

    public OdricsOutrider() {
        var deathTrigger = target(TargetFilters.creatureYouControl());
        var counterEffect = new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1);
        deathTrigger.addTrigger(Triggers.creatureDies(ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL), counterEffect);
    }
}
