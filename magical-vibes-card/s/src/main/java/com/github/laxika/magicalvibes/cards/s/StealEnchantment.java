package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TMP", collectorNumber = "90")
public class StealEnchantment extends Card {

    public StealEnchantment() {
        target(TargetFilters.enchantment())
                .addEffect(EffectSlot.STATIC, new ControlEnchantedCreatureEffect());
    }
}
