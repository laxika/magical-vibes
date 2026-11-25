package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

@CardRegistration(set = "POR", collectorNumber = "159")
@CardRegistration(set = "P02", collectorNumber = "124")
@CardRegistration(set = "ME4", collectorNumber = "144")
public class BeeSting extends Card {

    public BeeSting() {
        addSpellEffect(DealDamageEffect.toAnyTarget(2));
    }
}
