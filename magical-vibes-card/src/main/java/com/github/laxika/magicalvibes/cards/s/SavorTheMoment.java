package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "SHM", collectorNumber = "50")
public class SavorTheMoment extends Card {

    public SavorTheMoment() {
        // "Take an extra turn after this one. Skip the untap step of that turn."
        addEffect(EffectSlot.SPELL, new ControllerExtraTurnEffect(1, true));
    }
}
