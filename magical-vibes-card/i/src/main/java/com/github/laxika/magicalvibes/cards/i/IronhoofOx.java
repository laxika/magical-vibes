package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedByAtMostNCreaturesEffect;

@CardRegistration(set = "P02", collectorNumber = "130")
@CardRegistration(set = "ME4", collectorNumber = "158")
public class IronhoofOx extends Card {

    public IronhoofOx() {
        addStaticEffect(new CanBeBlockedByAtMostNCreaturesEffect(1));
    }
}
