package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "P02", collectorNumber = "54")
public class TemporalManipulation extends Card {

    public TemporalManipulation() {
        // "Take an extra turn after this one."
        addEffect(EffectSlot.SPELL, new ControllerExtraTurnEffect(1));
    }
}
