package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

@CardRegistration(set = "BRO", collectorNumber = "148")
public class PyrrhicBlast extends Card {

    public PyrrhicBlast() {
        addCastingCost(new SacrificeCreatureCost(false, true));
        addSpellEffect(DealDamageEffect.toAnyTarget(new XValue()));
        addSpellEffect(new DrawCardEffect());
    }
}
