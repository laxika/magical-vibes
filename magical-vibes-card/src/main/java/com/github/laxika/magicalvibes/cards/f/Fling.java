package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

@CardRegistration(set = "M11", collectorNumber = "139")
public class Fling extends Card {

    public Fling() {
        addEffect(EffectSlot.SPELL, new SacrificeCreatureCost(false, true));
        addEffect(EffectSlot.SPELL, new DealXDamageToAnyTargetEffect());
    }
}
