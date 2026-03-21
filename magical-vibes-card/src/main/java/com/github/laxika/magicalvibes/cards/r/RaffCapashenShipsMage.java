package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "DOM", collectorNumber = "202")
public class RaffCapashenShipsMage extends Card {

    public RaffCapashenShipsMage() {
        addEffect(EffectSlot.STATIC, new GrantFlashToCardTypeEffect(new CardIsHistoricPredicate()));
    }
}
