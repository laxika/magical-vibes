package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToDamageDealtEffect;

public class SpiritLink extends Card {

    public SpiritLink() {
        setNeedsTarget(true);
        addEffect(EffectSlot.STATIC, new GainLifeEqualToDamageDealtEffect());
    }
}
