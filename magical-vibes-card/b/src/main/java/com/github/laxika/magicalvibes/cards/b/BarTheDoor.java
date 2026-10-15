package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "DKA", collectorNumber = "2")
public class BarTheDoor extends Card {

    public BarTheDoor() {
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(0, 4));
    }
}
