package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "MSC", collectorNumber = "525")
public class ValkyriorSkyrider extends Card {

    public ValkyriorSkyrider() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(4));
    }
}
