package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "10E", collectorNumber = "232")
public class Shock extends Card {

    public Shock() {
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(2));
    }
}
