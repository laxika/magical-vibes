package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyBlockedCreatureAndSelfEffect;

public class LoyalSentry extends Card {

    public LoyalSentry() {
        addEffect(EffectSlot.ON_BLOCK, new DestroyBlockedCreatureAndSelfEffect());
    }
}
