package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "M13", collectorNumber = "147")
public class SearingSpear extends Card {

    public SearingSpear() {
        // "Searing Spear deals 3 damage to any target."
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3));
    }
}
