package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EVE", collectorNumber = "157")
public class OverbeingOfMyth extends Card {

    public OverbeingOfMyth() {
        CardsInHand cardsInHand = new CardsInHand(CountScope.CONTROLLER);
        addStaticEffect(new SetPowerToughnessToAmountEffect(cardsInHand, cardsInHand));
        addTrigger(Triggers.drawStep(ActivePlayer.YOU), new DrawCardEffect(1));
    }
}
