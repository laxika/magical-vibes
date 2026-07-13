package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;

@CardRegistration(set = "9ED", collectorNumber = "254")
@CardRegistration(set = "6ED", collectorNumber = "241")
@CardRegistration(set = "8ED", collectorNumber = "264")
@CardRegistration(set = "7ED", collectorNumber = "256")
public class Maro extends Card {

    public Maro() {
        CardsInHand cardsInHand = new CardsInHand(CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(cardsInHand, cardsInHand));
    }
}
