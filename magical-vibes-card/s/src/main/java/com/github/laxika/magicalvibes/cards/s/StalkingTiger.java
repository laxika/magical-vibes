package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedByAtMostNCreaturesEffect;

@CardRegistration(set = "10E", collectorNumber = "299")
@CardRegistration(set = "POR", collectorNumber = "186")
@CardRegistration(set = "PTK", collectorNumber = "149")
@CardRegistration(set = "6ED", collectorNumber = "253")
@CardRegistration(set = "MIR", collectorNumber = "243")
@CardRegistration(set = "W17", collectorNumber = "28")
public class StalkingTiger extends Card {

    public StalkingTiger() {
        addStaticEffect(new CanBeBlockedByAtMostNCreaturesEffect(1));
    }
}
