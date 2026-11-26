package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M14", collectorNumber = "13")
@CardRegistration(set = "ORI", collectorNumber = "9")
public class ChargingGriffin extends Card {

    public ChargingGriffin() {
        // Flying (keyword auto-loaded from Scryfall)
        // Whenever this creature attacks, it gets +1/+1 until end of turn.
        addTrigger(Triggers.attacks(), BoostEffect.self(1, 1));
    }
}
