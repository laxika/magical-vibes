package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "NPH", collectorNumber = "86")
public class GutShot extends Card {

    public GutShot() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(1));
    }
}
