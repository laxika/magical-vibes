package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RAV", collectorNumber = "89")
@CardRegistration(set = "MAR", collectorNumber = "17")
@CardRegistration(set = "OMB", collectorNumber = "17")
public class Hex extends Card {

    public Hex() {
        target(TargetFilters.creature(), 6, 6)
                .addEffect(EffectSlot.SPELL, new DestroyEachTargetPermanentEffect());
    }
}
