package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "IKO", collectorNumber = "42")
public class AvianOddity extends Card {

    public AvianOddity() {
        target(TargetFilters.creatureYouControl()).addTrigger(Triggers.youCycleThisCard(),
                new PutCounterOnTargetPermanentEffect(CounterType.FLYING, 1));
        addCycling("{2}{U}");
    }
}
