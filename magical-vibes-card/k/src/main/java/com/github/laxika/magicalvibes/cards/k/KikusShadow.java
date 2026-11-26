package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

@CardRegistration(set = "SOK", collectorNumber = "77")
public class KikusShadow extends Card {

    public KikusShadow() {
        addSpellEffect(DealDamageEffect.targetCreatureToItself(1));
    }
}
