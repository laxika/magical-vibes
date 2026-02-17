package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;

public class BogardanFirefiend extends Card {

    public BogardanFirefiend() {
        addEffect(EffectSlot.ON_DEATH, new DealDamageToTargetCreatureEffect(2));
    }
}
