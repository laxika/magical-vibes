package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SPM", collectorNumber = "107")
@CardRegistration(set = "OM1", collectorNumber = "109")
@CardRegistration(set = "MSC", collectorNumber = "816")
public class LurkingLizards extends Card {

    public LurkingLizards() {
        addTrigger(Triggers.youCastSpell()
                        .withStackEntryFilter(new StackEntryNotPredicate(new StackEntryMaxManaValuePredicate(3))),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
