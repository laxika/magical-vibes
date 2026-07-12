package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "SHM", collectorNumber = "92")
public class FlameJavelin extends Card {

    public FlameJavelin() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(4));
    }
}
