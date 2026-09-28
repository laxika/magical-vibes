package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;

@CardRegistration(set = "MSC", collectorNumber = "723")
public class HitMonkey extends Card {

    public HitMonkey() {
        addEffect(EffectSlot.STATIC, new CantBeCounteredEffect());
    }
}
