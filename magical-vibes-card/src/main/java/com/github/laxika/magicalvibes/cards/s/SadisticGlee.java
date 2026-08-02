package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TMP", collectorNumber = "153")
public class SadisticGlee extends Card {

    public SadisticGlee() {
        target(TargetFilters.creature())
                // Whenever a creature dies, put a +1/+1 counter on enchanted creature.
                .addEffect(EffectSlot.ON_ANY_CREATURE_DIES,
                        new PutCounterOnEnchantedCreatureEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
