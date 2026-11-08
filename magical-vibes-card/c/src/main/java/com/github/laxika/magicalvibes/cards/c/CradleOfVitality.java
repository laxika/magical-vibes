package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ALA", collectorNumber = "7")
@CardRegistration(set = "C13", collectorNumber = "7")
public class CradleOfVitality extends Card {

    public CradleOfVitality() {
        // Whenever you gain life, you may pay {1}{W}. If you do, put a +1/+1 counter on target
        // creature for each 1 life you gained.
        addTrigger(Triggers.youGainLife(), new MayPayManaEffect("{1}{W}",
                PutCounterOnTargetPermanentEffect.withTargetRestriction(CounterType.PLUS_ONE_PLUS_ONE,
                        new EventValue(), new PermanentIsCreaturePredicate()),
                "Pay {1}{W} to put a +1/+1 counter on target creature for each 1 life you gained?"));
    }
}
