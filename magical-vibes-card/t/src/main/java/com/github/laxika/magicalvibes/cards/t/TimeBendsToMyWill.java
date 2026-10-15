package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;

@CardRegistration(set = "DSC", collectorNumber = "357")
public class TimeBendsToMyWill extends Card {

    public TimeBendsToMyWill() {
        addEffect(EffectSlot.SPELL, new ControllerExtraTurnEffect(1, true));
    }
}
