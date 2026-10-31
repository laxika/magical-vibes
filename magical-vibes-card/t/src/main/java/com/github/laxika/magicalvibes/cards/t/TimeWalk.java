package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;

@CardRegistration(set = "VMA", collectorNumber = "2")
@CardRegistration(set = "2ED", collectorNumber = "84")
@CardRegistration(set = "LEA", collectorNumber = "83")
@CardRegistration(set = "LEB", collectorNumber = "84")
public class TimeWalk extends Card {

    public TimeWalk() {
        addSpellEffect(new ControllerExtraTurnEffect(1));
    }
}
