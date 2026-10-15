package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

@CardRegistration(set = "M15", collectorNumber = "88")
public class CarrionCrow extends Card {

    public CarrionCrow() {
        // This creature enters the battlefield tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
    }
}
