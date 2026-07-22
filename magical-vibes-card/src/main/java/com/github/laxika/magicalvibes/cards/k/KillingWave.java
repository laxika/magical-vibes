package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.KillingWaveEffect;

@CardRegistration(set = "INR", collectorNumber = "121")
public class KillingWave extends Card {

    public KillingWave() {
        addEffect(EffectSlot.SPELL, new KillingWaveEffect());
    }
}
