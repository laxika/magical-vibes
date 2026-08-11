package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReduceControllerMaxHandSizeEffect;

@CardRegistration(set = "ODY", collectorNumber = "107")
public class ThoughtNibbler extends Card {

    public ThoughtNibbler() {
        addEffect(EffectSlot.STATIC, new ReduceControllerMaxHandSizeEffect(2));
    }
}
