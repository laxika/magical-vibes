package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.amount.SuspendedCards;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;

@CardRegistration(set = "WHO", collectorNumber = "5")
@CardRegistration(set = "WHO", collectorNumber = "346")
public class RoseTyler extends Card {

    public RoseTyler() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.SELF, CounterType.TIME));
        addEffect(EffectSlot.ON_ATTACK, new PutCountersOnSelfEffect(
                CounterType.TIME,
                new Sum(
                        new SuspendedCards(CountScope.CONTROLLER),
                        new PermanentCount(
                                new PermanentHasCountersPredicate(CounterType.TIME),
                                CountScope.CONTROLLER,
                                true))));
    }
}
