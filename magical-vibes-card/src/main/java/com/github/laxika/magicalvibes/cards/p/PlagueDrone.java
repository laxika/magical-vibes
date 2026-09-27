package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.OpponentLifeGainBecomesLifeLossEffect;

@CardRegistration(set = "40K", collectorNumber = "47")
public class PlagueDrone extends Card {

    public PlagueDrone() {
        addEffect(EffectSlot.STATIC, new OpponentLifeGainBecomesLifeLossEffect());
    }
}
