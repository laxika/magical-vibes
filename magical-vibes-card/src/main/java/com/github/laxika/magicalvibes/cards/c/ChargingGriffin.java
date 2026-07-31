package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "M14", collectorNumber = "13")
public class ChargingGriffin extends Card {

    public ChargingGriffin() {
        // Flying (keyword auto-loaded from Scryfall)
        // Whenever this creature attacks, it gets +1/+1 until end of turn.
        addEffect(EffectSlot.ON_ATTACK, new BoostSelfEffect(1, 1));
    }
}
