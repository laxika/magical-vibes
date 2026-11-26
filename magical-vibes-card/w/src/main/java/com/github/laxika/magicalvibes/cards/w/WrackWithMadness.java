package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

@CardRegistration(set = "DKA", collectorNumber = "107")
public class WrackWithMadness extends Card {

    public WrackWithMadness() {
        addSpellEffect(DealDamageEffect.targetCreatureToItself(1));
    }
}
