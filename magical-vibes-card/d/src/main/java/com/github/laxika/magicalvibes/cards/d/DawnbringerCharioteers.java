package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsSourcePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "JOU", collectorNumber = "6")
public class DawnbringerCharioteers extends Card {

    public DawnbringerCharioteers() {
        addTrigger(Triggers.youCastSpell().withStackEntryFilter(new StackEntryTargetsSourcePredicate()),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
