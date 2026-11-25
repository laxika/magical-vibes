package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

public class HeartflameSlash extends Card {

    public HeartflameSlash() {
        addSpellEffect(DealDamageEffect.toAnyTarget(3));
    }
}
