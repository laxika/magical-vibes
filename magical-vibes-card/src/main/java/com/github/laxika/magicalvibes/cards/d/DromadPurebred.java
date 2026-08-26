package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "RAV", collectorNumber = "15")
public class DromadPurebred extends Card {

    public DromadPurebred() {
        addEffect(EffectSlot.ON_DEALT_DAMAGE, new GainLifeEffect(1));
    }
}
