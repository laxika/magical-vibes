package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardAllCardsOfChosenColorEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INV", collectorNumber = "242")
@CardRegistration(set = "PD3", collectorNumber = "5")
@CardRegistration(set = "C17", collectorNumber = "168")
public class CrosisThePurger extends Card {

    public CrosisThePurger() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                UnlessPaysEffect.youMayPay("{2}{B}", DiscardAllCardsOfChosenColorEffect.damagedPlayer()));
    }
}
