package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostEnchantedCreatureByColorCountEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DIS", collectorNumber = "4")
public class BlessingOfTheNephilim extends Card {

    public BlessingOfTheNephilim() {
        target(TargetFilters.creature());
        addEffect(EffectSlot.STATIC, new BoostEnchantedCreatureByColorCountEffect(1, 1));
    }
}
