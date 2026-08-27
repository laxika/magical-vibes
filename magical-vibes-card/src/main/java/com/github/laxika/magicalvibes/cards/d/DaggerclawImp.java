package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;

@CardRegistration(set = "GPT", collectorNumber = "48")
public class DaggerclawImp extends Card {

    public DaggerclawImp() {
        addEffect(EffectSlot.STATIC, new CantBlockEffect());
    }
}
