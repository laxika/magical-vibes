package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

@CardRegistration(set = "FRF", collectorNumber = "95")
public class CollateralDamage extends Card {

    public CollateralDamage() {
        addCastingCost(new SacrificeCreatureCost());
        addSpellEffect(DealDamageEffect.toAnyTarget(3));
    }
}
