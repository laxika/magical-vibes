package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "M13", collectorNumber = "9")
public class CaptainsCall extends Card {

    public CaptainsCall() {
        // Create three 1/1 white Soldier creature tokens.
        addEffect(EffectSlot.SPELL, CreateTokenEffect.whiteSoldier(3));
    }
}
