package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "1418")
@CardRegistration(set = "SLD", collectorNumber = "1766")
@CardRegistration(set = "2XM", collectorNumber = "209")
@CardRegistration(set = "SOC", collectorNumber = "319")
@CardRegistration(set = "C15", collectorNumber = "48")
@CardRegistration(set = "EOC", collectorNumber = "122")
@CardRegistration(set = "CMA", collectorNumber = "185")
public class MazirekKraulDeathPriest extends Card {

    public MazirekKraulDeathPriest() {
        addTrigger(Triggers.sacrificed(null, ObjectRelation.ANOTHER),
                new PutCounterOnEachControlledPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate()));
    }
}
