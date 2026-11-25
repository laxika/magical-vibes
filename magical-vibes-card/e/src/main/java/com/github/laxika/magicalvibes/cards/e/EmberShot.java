package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "JUD", collectorNumber = "87")
public class EmberShot extends Card {

    public EmberShot() {
        addSpellEffect(DealDamageEffect.toAnyTarget(3));
        addSpellEffect(new DrawCardEffect(1));
    }
}
