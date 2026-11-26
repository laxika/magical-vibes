package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;

public class VisionOfTheUnspeakable extends Card {

    public VisionOfTheUnspeakable() {
        CardsInHand handSize = new CardsInHand(CountScope.CONTROLLER);
        addStaticEffect(BoostEffect.self(handSize, handSize));
    }
}
