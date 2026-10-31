package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

public class VisionOfTheUnspeakable extends Card {

    public VisionOfTheUnspeakable() {
        CardsInHand handSize = new CardsInHand(CountScope.CONTROLLER);
        addStaticEffect(new BoostSelfEffect(handSize, handSize));
    }
}
