package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.KrarksThumbEffect;

@CardRegistration(set = "MRD", collectorNumber = "190")
public class KrarksThumb extends Card {

    public KrarksThumb() {
        addEffect(EffectSlot.STATIC, new KrarksThumbEffect());
    }
}
