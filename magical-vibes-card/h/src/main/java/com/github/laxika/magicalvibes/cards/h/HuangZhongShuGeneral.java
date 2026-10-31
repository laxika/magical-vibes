package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedByAtMostNCreaturesEffect;

@CardRegistration(set = "PTK", collectorNumber = "8")
public class HuangZhongShuGeneral extends Card {

    public HuangZhongShuGeneral() {
        addStaticEffect(new CanBeBlockedByAtMostNCreaturesEffect(1));
    }
}
