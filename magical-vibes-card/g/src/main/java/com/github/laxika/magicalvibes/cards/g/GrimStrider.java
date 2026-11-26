package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Scaled;

@CardRegistration(set = "AKH", collectorNumber = "94")
public class GrimStrider extends Card {

    public GrimStrider() {
        // This creature gets -1/-1 for each card in your hand.
        Scaled minusPerCard = new Scaled(new CardsInHand(CountScope.CONTROLLER), -1);
        addStaticEffect(BoostEffect.self(minusPerCard, minusPerCard));
    }
}
