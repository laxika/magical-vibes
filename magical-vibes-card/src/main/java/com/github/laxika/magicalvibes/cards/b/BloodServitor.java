package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "VOW", collectorNumber = "252")
public class BloodServitor extends Card {

    public BloodServitor() {
        // When this creature enters, create a Blood token.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.ofBloodToken(1));
    }
}
