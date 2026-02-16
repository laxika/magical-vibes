package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToAnyTargetEffect;

public class Blaze extends Card {

    public Blaze() {
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new DealXDamageToAnyTargetEffect());
    }
}
