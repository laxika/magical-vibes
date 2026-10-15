package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedIfAbleEffect;

@CardRegistration(set = "SHM", collectorNumber = "95")
public class InescapableBrute extends Card {

    public InescapableBrute() {
        // Wither is auto-loaded from the keyword line and handled by the engine.
        addEffect(EffectSlot.STATIC, new MustBeBlockedIfAbleEffect());
    }
}
