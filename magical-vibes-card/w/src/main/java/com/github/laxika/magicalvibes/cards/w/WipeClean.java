package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SCG", collectorNumber = "26")
public class WipeClean extends Card {

    public WipeClean() {
        target(TargetFilters.enchantment())
                .addEffect(EffectSlot.SPELL, new ExileTargetPermanentEffect());
        addCycling("{3}");
    }
}
