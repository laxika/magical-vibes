package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "DIS", collectorNumber = "109")
public class DreadSlag extends Card {

    public DreadSlag() {
        CardsInHand cardsInHand = new CardsInHand(CountScope.CONTROLLER);
        Scaled minusFourPerCard = new Scaled(cardsInHand, -4);
        addEffect(EffectSlot.STATIC, new DynamicStaticBoostEffect(
                minusFourPerCard, minusFourPerCard, GrantScope.SELF));
    }
}
