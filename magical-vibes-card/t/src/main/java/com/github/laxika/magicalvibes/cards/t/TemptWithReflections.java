package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TemptingOfferCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "C13", collectorNumber = "60")
public class TemptWithReflections extends Card {

    public TemptWithReflections() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.SPELL, new TemptingOfferCreateTokenCopyEffect());
    }
}
