package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

@CardRegistration(set = "KLD", collectorNumber = "266")
public class FlameLash extends Card {

    public FlameLash() {
        addSpellEffect(DealDamageEffect.toAnyTarget(4));
    }
}
