package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.PartySize;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "ZNR", collectorNumber = "38")
public class ShepherdOfHeroes extends Card {

    public ShepherdOfHeroes() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new GainLifeEffect(new Scaled(new PartySize(), 2)));
    }
}
