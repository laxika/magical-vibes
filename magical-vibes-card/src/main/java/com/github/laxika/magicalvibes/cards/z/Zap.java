package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "INV", collectorNumber = "180")
public class Zap extends Card {

    public Zap() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(1));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
