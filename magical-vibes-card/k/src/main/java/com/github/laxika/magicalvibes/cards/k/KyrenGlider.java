package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;

@CardRegistration(set = "MMQ", collectorNumber = "196")
public class KyrenGlider extends Card {

    public KyrenGlider() {
        addEffect(EffectSlot.STATIC, new CantBlockEffect());
    }
}
