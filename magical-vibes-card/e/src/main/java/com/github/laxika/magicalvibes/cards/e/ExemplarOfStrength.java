package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CounterRemovalSubject;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterAndGainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AKH", collectorNumber = "165")
@CardRegistration(set = "AKR", collectorNumber = "190")
public class ExemplarOfStrength extends Card {

    public ExemplarOfStrength() {
        // "When this creature enters, put three -1/-1 counters on target creature you control."
        target(TargetFilters.creatureYouControl()).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 3));

        // "Whenever this creature attacks, remove a -1/-1 counter from it. If you do, you gain 1 life."
        addTrigger(Triggers.attacks(),
                new RemoveCounterAndGainLifeEffect(CounterType.MINUS_ONE_MINUS_ONE, 1, CounterRemovalSubject.SOURCE));
    }
}
