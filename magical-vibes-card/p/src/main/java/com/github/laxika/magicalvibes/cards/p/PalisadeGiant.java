package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RedirectPlayerDamageToSelfEffect;

@CardRegistration(set = "RTR", collectorNumber = "15")
public class PalisadeGiant extends Card {

    public PalisadeGiant() {
        // "All damage that would be dealt to you and other permanents you control is dealt to this
        // creature instead." — the player half plus the other-permanents half of the redirect.
        addEffect(EffectSlot.STATIC, new RedirectPlayerDamageToSelfEffect(true));
    }
}
