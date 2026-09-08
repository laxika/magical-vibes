package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "APC", collectorNumber = "126")
public class Vindicate extends Card {

    public Vindicate() {
        target(TargetFilters.permanent())
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }
}
