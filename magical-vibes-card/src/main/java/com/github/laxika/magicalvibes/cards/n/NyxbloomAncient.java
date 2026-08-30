package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ManaReflectionEffect;

@CardRegistration(set = "THB", collectorNumber = "190")
public class NyxbloomAncient extends Card {

    public NyxbloomAncient() {
        addEffect(EffectSlot.STATIC, new ManaReflectionEffect(3));
    }
}
