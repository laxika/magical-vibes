package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RiotEffect;

@CardRegistration(set = "RNA", collectorNumber = "150")
public class WreckingBeast extends Card {

    public WreckingBeast() {
        addStaticEffect(new RiotEffect());
    }
}
