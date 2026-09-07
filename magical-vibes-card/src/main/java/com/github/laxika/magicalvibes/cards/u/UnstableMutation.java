package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "4ED", collectorNumber = "110")
@CardRegistration(set = "5ED", collectorNumber = "131")
@CardRegistration(set = "SUM", collectorNumber = "86")
@CardRegistration(set = "TSB", collectorNumber = "33")
public class UnstableMutation extends Card {

    public UnstableMutation() {
        target(TargetFilters.creature())
                // Enchanted creature gets +3/+3.
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(3, 3, GrantScope.ENCHANTED_CREATURE))

                // At the beginning of the upkeep of enchanted creature's controller,
                // put a -1/-1 counter on that creature.
                .addEffect(EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED,
                        new PutCounterOnReferencedPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE));
    }
}
