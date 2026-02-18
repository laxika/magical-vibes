package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

public class Incinerate extends Card {

    public Incinerate() {
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3, true));
    }
}
