package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

public class EmberethBlaze extends Card {

    public EmberethBlaze() {
        addSpellEffect(new DealDamageToAnyTargetEffect(2));
    }
}
