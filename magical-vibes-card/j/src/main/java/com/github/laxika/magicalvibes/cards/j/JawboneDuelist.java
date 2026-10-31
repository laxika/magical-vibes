package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ToxicEffect;

@CardRegistration(set = "ONE", collectorNumber = "18")
public class JawboneDuelist extends Card {

    public JawboneDuelist() {
        addStaticEffect(new ToxicEffect(1));
    }
}
