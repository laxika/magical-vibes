package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageFromCreaturesEffect;

@CardRegistration(set = "MSC", collectorNumber = "510")
public class AbsorbingManAndTitania extends Card {

    public AbsorbingManAndTitania() {
        addEffect(EffectSlot.STATIC, new DoubleDamageFromCreaturesEffect());
    }
}
