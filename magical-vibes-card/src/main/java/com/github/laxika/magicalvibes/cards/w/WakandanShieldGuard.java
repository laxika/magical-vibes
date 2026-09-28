package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "MSC", collectorNumber = "613")
public class WakandanShieldGuard extends Card {

    public WakandanShieldGuard() {
        // When this creature enters, create a 1/1 white Soldier creature token.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.whiteSoldier(1));
    }
}
