package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;

@CardRegistration(set = "SHM", collectorNumber = "180")
public class AshenmoorGouger extends Card {

    public AshenmoorGouger() {
        // "This creature can't block."
        addEffect(EffectSlot.STATIC, new CantBlockEffect());
    }
}
