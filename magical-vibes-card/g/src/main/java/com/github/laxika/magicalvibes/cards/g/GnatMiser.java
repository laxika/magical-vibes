package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReduceOpponentMaxHandSizeEffect;

@CardRegistration(set = "SOK", collectorNumber = "70")
public class GnatMiser extends Card {

    public GnatMiser() {
        addStaticEffect(new ReduceOpponentMaxHandSizeEffect(1));
    }
}
