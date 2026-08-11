package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "INV", collectorNumber = "52")
public class DistortingWake extends Card {

    public DistortingWake() {
        targetX(TargetFilters.nonlandPermanent(), 100)
                .addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());
    }
}
