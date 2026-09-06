package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FUT", collectorNumber = "98")
public class FatalAttraction extends Card {

    public FatalAttraction() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DealDamageToEnchantedCreatureEffect(2))
                .addEffect(EffectSlot.UPKEEP_TRIGGERED, new DealDamageToEnchantedCreatureEffect(4));
    }
}
