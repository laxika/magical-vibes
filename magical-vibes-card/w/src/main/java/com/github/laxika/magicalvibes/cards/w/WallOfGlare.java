package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CanBlockAnyNumberOfCreaturesEffect;

@CardRegistration(set = "UDS", collectorNumber = "25")
public class WallOfGlare extends Card {

    public WallOfGlare() {
        addStaticEffect(new CanBlockAnyNumberOfCreaturesEffect());
    }
}
