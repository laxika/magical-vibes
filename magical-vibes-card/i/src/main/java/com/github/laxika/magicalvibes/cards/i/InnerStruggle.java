package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

@CardRegistration(set = "SOI", collectorNumber = "167")
public class InnerStruggle extends Card {

    public InnerStruggle() {
        addSpellEffect(DealDamageEffect.targetCreatureToItself(1));
    }
}
