package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M20", collectorNumber = "51")
public class CaptivatingGyre extends Card {

    public CaptivatingGyre() {
        target(TargetFilters.creature(), 0, 3).addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());
    }
}
