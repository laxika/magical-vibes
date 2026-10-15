package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "OTJ", collectorNumber = "29")
public class SheriffOfSafePassage extends Card {

    public SheriffOfSafePassage() {
        // This creature enters with a +1/+1 counter on it plus an additional +1/+1 counter on it
        // for each other creature you control.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithCountersEffect(
                CounterType.PLUS_ONE_PLUS_ONE,
                new Sum(
                        new Fixed(1),
                        new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER, true)
                )
        ));
    }
}
