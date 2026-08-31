package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "9ED", collectorNumber = "59")
@CardRegistration(set = "ONS", collectorNumber = "63")
public class Annex extends Card {

    public Annex() {
        target(TargetFilters.land()).addEffect(EffectSlot.STATIC, new ControlEnchantedCreatureEffect());
    }
}
