package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.PermanentCounterSum;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WAR", collectorNumber = "186")
public class BioessenceHydra extends Card {

    public BioessenceHydra() {
        addStaticEffect(new EnterWithCountersEffect(
                CounterType.PLUS_ONE_PLUS_ONE,
                new PermanentCounterSum(
                        CounterType.LOYALTY,
                        new PermanentIsPlaneswalkerPredicate(),
                        CountScope.CONTROLLER)));
        addTrigger(Triggers.loyaltyCountersPutOnPlaneswalkersYouControl(),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, new EventValue()));
    }
}
