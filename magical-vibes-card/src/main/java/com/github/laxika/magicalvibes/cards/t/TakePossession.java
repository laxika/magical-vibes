package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FUT", collectorNumber = "44")
public class TakePossession extends Card {

    public TakePossession() {
        target(TargetFilters.permanent())
                .addEffect(EffectSlot.STATIC, new ControlEnchantedCreatureEffect());
    }
}
