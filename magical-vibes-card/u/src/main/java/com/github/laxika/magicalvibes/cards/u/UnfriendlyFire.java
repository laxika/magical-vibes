package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "XLN", collectorNumber = "172")
public class UnfriendlyFire extends Card {

    public UnfriendlyFire() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(4));
    }
}
