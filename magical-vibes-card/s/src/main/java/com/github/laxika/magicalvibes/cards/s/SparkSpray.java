package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

@CardRegistration(set = "SCG", collectorNumber = "105")
@CardRegistration(set = "VMA", collectorNumber = "188")
@CardRegistration(set = "DMR", collectorNumber = "141")
public class SparkSpray extends Card {

    public SparkSpray() {
        addSpellEffect(DealDamageEffect.toAnyTarget(1));
        addCycling("{R}");
    }
}
