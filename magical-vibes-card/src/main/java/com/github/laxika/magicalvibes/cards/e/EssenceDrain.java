package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetAndGainLifeEffect;

public class EssenceDrain extends Card {

    public EssenceDrain() {
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetAndGainLifeEffect(3, 3));
    }
}
