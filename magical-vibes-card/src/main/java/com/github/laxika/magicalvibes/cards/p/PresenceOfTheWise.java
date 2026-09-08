package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "SOK", collectorNumber = "23")
public class PresenceOfTheWise extends Card {

    public PresenceOfTheWise() {
        addEffect(EffectSlot.SPELL, new GainLifeEffect(new Scaled(new CardsInHand(CountScope.CONTROLLER), 2)));
    }
}
