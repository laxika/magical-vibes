package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "S99", collectorNumber = "93")
public class CinderStorm extends Card {

    public CinderStorm() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(7));
    }
}
