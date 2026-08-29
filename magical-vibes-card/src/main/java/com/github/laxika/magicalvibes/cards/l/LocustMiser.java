package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReduceOpponentMaxHandSizeEffect;

@CardRegistration(set = "SOK", collectorNumber = "80")
public class LocustMiser extends Card {

    public LocustMiser() {
        addEffect(EffectSlot.STATIC, new ReduceOpponentMaxHandSizeEffect(2));
    }
}
