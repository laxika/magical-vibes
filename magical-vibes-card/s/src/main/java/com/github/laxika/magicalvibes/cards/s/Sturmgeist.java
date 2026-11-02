package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ISD", collectorNumber = "82")
public class Sturmgeist extends Card {

    public Sturmgeist() {
        CardsInHand cardsInHand = new CardsInHand(CountScope.CONTROLLER);
        addStaticEffect(new SetPowerToughnessToAmountEffect(cardsInHand, cardsInHand));
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new DrawCardEffect());
    }
}
