package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SOI", collectorNumber = "16")
public class EerieInterlude extends Card {

    public EerieInterlude() {
        target(TargetFilters.creatureYouControl(), 0, 99)
                .addEffect(EffectSlot.SPELL, FlickerEffect.exileTargetReturnAtEndStep());
    }
}
