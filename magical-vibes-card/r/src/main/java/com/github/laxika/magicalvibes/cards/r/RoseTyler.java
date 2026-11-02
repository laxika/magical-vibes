package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.amount.SuspendedCards;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "5")
@CardRegistration(set = "WHO", collectorNumber = "346")
@CardRegistration(set = "WHO", collectorNumber = "535")
@CardRegistration(set = "WHO", collectorNumber = "610")
@CardRegistration(set = "WHO", collectorNumber = "937")
@CardRegistration(set = "WHO", collectorNumber = "1126")
public class RoseTyler extends Card {

    public RoseTyler() {
        addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.SELF, CounterType.TIME));
        addTrigger(Triggers.attacks(), new PutCountersOnSelfEffect(
                CounterType.TIME,
                new Sum(
                        new SuspendedCards(CountScope.CONTROLLER),
                        new PermanentCount(
                                new PermanentHasCountersPredicate(CounterType.TIME),
                                CountScope.CONTROLLER,
                                true))));
    }
}
