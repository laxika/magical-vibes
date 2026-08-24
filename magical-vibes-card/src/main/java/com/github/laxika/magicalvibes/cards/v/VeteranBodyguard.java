package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RedirectPlayerDamageToSelfEffect;

@CardRegistration(set = "SUM", collectorNumber = "42")
public class VeteranBodyguard extends Card {

    public VeteranBodyguard() {
        addEffect(EffectSlot.STATIC, new RedirectPlayerDamageToSelfEffect(false, true));
    }
}
