package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

@CardRegistration(set = "M19", collectorNumber = "163")
public class Thud extends Card {

    public Thud() {
        addCastingCost(new SacrificeCreatureCost(false, true));
        addSpellEffect(DealDamageEffect.toAnyTarget(new XValue()));
    }
}
