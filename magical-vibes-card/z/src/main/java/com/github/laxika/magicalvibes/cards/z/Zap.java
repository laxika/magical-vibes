package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "INV", collectorNumber = "180")
public class Zap extends Card {

    public Zap() {
        addSpellEffect(DealDamageEffect.toAnyTarget(1));
        addSpellEffect(new DrawCardEffect(1));
    }
}
