package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;

@CardRegistration(set = "10E", collectorNumber = "180")
public class SpinelessThug extends Card {

    public SpinelessThug() {
        addEffect(EffectSlot.STATIC, new CantBlockEffect());
    }
}
