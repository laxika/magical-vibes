package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "CHK", collectorNumber = "39")
public class QuietPurity extends Card {

    public QuietPurity() {
        target(TargetFilters.enchantment())
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }
}
