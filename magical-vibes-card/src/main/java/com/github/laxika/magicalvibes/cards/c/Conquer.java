package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "6ED", collectorNumber = "171")
@CardRegistration(set = "5ED", collectorNumber = "216")
public class Conquer extends Card {

    public Conquer() {
        // Enchant land. You control enchanted land.
        target(TargetFilters.land())
                .addEffect(EffectSlot.STATIC, new ControlEnchantedCreatureEffect());
    }
}
