package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsPermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "596")
public class HawkeyeBowslinger extends Card {

    public HawkeyeBowslinger() {
        // Whenever you cast a spell that targets a creature, put a +1/+1 counter on Hawkeye. Scry 1.
        addTrigger(Triggers.youCastSpell()
                        .withStackEntryFilter(new StackEntryTargetsPermanentPredicate(new PermanentIsCreaturePredicate())),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE),
                new ScryEffect(1));
    }
}
