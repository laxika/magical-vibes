package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "ISD", collectorNumber = "22")
public class MidnightHaunting extends Card {

    public MidnightHaunting() {
        // Create two 1/1 white Spirit creature tokens with flying.
        addEffect(EffectSlot.SPELL, CreateTokenEffect.whiteSpirit(2));
    }
}
