package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

public class EmberethBlaze extends Card {

    public EmberethBlaze() {
        addSpellEffect(DealDamageEffect.toAnyTarget(2));
    }
}
