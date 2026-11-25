package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

@CardRegistration(set = "M11", collectorNumber = "181")
public class HornetSting extends Card {

    public HornetSting() {
        addSpellEffect(DealDamageEffect.toAnyTarget(1));
    }
}
