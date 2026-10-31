package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ToxicEffect;

@CardRegistration(set = "ONE", collectorNumber = "188")
public class TyrranaxAtrocity extends Card {

    public TyrranaxAtrocity() {
        addStaticEffect(new ToxicEffect(3));
    }
}
