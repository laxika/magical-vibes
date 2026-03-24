package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "XLN", collectorNumber = "202")
public class RavenousDaggertooth extends Card {

    public RavenousDaggertooth() {
        // Enrage — Whenever this creature is dealt damage, you gain 2 life.
        addEffect(EffectSlot.ON_DEALT_DAMAGE, new GainLifeEffect(2));
    }
}
