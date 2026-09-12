package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;

@CardRegistration(set = "VMA", collectorNumber = "2")
public class TimeWalk extends Card {

    public TimeWalk() {
        addEffect(EffectSlot.SPELL, new ControllerExtraTurnEffect(1));
    }
}
