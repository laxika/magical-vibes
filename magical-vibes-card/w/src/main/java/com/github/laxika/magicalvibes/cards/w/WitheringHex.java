package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONS", collectorNumber = "181")
public class WitheringHex extends Card {

    public WitheringHex() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(-1, -1, GrantScope.ENCHANTED_CREATURE, CounterType.PLAGUE));
        addTrigger(Triggers.playerCyclesCard(), new PutCountersOnSelfEffect(CounterType.PLAGUE));
    }
}
