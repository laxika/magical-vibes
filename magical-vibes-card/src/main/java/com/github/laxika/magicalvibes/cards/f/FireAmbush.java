package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "PTK", collectorNumber = "111")
public class FireAmbush extends Card {

    public FireAmbush() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3));
    }
}
