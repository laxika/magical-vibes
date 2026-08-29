package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "MMQ", collectorNumber = "241")
public class DeepwoodTantiv extends Card {

    public DeepwoodTantiv() {
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new GainLifeEffect(2));
    }
}
