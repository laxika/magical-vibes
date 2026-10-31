package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;

@CardRegistration(set = "5DN", collectorNumber = "69")
public class IronBarbHellion extends Card {

    public IronBarbHellion() {
        addStaticEffect(new CantBlockEffect());
    }
}
