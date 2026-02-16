package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

public class VenerableMonk extends Card {

    public VenerableMonk() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(2));
    }
}
