package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "IKO", collectorNumber = "145")
public class BarrierBreach extends Card {

    public BarrierBreach() {
        target(TargetFilters.enchantment(), 0, 3)
                .addEffect(EffectSlot.SPELL, new ExileTargetPermanentEffect());
        addCycling("{2}");
    }
}
