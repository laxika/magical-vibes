package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "POR", collectorNumber = "11")
@CardRegistration(set = "EXO", collectorNumber = "4")
@CardRegistration(set = "TPR", collectorNumber = "9")
@CardRegistration(set = "S99", collectorNumber = "12")
@CardRegistration(set = "DDC", collectorNumber = "4")
@CardRegistration(set = "DVD", collectorNumber = "4")
public class ChargingPaladin extends Card {

    public ChargingPaladin() {
        addTrigger(Triggers.attacks(), new BoostSelfEffect(0, 3));
    }
}
