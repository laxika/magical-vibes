package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "BNG", collectorNumber = "89")
public class BoltOfKeranos extends Card {

    public BoltOfKeranos() {
        addSpellEffect(DealDamageEffect.toAnyTarget(3));
        addSpellEffect(new ScryEffect(1));
    }
}
