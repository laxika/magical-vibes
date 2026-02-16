package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExtraTurnEffect;

public class TimeStretch extends Card {

    public TimeStretch() {
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new ExtraTurnEffect(2));
    }
}
