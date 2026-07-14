package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;

@CardRegistration(set = "P02", collectorNumber = "98")
@CardRegistration(set = "8ED", collectorNumber = "189")
@CardRegistration(set = "7ED", collectorNumber = "189")
public class GoblinGlider extends Card {

    public GoblinGlider() {
        addEffect(EffectSlot.STATIC, new CantBlockEffect());
    }
}
