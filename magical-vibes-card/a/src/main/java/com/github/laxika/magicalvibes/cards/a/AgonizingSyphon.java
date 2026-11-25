package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "M20", collectorNumber = "83")
public class AgonizingSyphon extends Card {

    public AgonizingSyphon() {
        addSpellEffect(DealDamageEffect.toAnyTarget(3));
        addSpellEffect(new GainLifeEffect(3));
    }
}
