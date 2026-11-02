package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.BecomeDayAsEntersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MID", collectorNumber = "244")
@CardRegistration(set = "DBL", collectorNumber = "244")
public class SunriseCavalier extends Card {

    public SunriseCavalier() {
        addStaticEffect(new BecomeDayAsEntersEffect());
        target(TargetFilters.creatureYouControl()).addTrigger(Triggers.dayBecomesNightOrNightBecomesDay(),
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));
    }
}
