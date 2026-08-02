package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "GTC", collectorNumber = "120")
public class ForcedAdaptation extends Card {

    public ForcedAdaptation() {
        target(TargetFilters.creature())
                // At the beginning of your upkeep, put a +1/+1 counter on enchanted creature.
                .addEffect(EffectSlot.UPKEEP_TRIGGERED, new PutCounterOnEnchantedCreatureEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
