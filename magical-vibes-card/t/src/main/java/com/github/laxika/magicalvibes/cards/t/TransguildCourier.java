package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "DIS", collectorNumber = "168")
@CardRegistration(set = "DMC", collectorNumber = "194")
public class TransguildCourier extends Card {

    public TransguildCourier() {
        for (CardColor color : CardColor.values()) {
            addStaticEffect(new GrantColorEffect(color, GrantScope.SELF));
        }
    }
}
