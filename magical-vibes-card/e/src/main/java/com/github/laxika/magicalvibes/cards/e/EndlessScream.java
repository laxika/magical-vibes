package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TMP", collectorNumber = "132")
public class EndlessScream extends Card {

    public EndlessScream() {
        target(TargetFilters.creature());

        // This Aura enters with X scream counters on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.SCREAM, new XValue()));

        // Enchanted creature gets +1/+0 for each scream counter on this Aura.
        addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(1, 0, GrantScope.ENCHANTED_CREATURE, CounterType.SCREAM));
    }
}
