package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

@CardRegistration(set = "FRF", collectorNumber = "95")
public class CollateralDamage extends Card {

    public CollateralDamage() {
        addEffect(EffectSlot.SPELL, new SacrificeCreatureCost());
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3));
    }
}
