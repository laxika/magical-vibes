package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

@CardRegistration(set = "TSP", collectorNumber = "214")
public class ScarwoodTreefolk extends Card {

    public ScarwoodTreefolk() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
    }
}
