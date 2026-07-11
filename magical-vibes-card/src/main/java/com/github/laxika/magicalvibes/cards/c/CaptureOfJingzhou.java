package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "PTK", collectorNumber = "38")
public class CaptureOfJingzhou extends Card {

    public CaptureOfJingzhou() {
        // "Take an extra turn after this one."
        addEffect(EffectSlot.SPELL, new ControllerExtraTurnEffect(1));
    }
}
