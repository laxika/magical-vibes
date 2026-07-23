package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnDamagedCreatureUnderControlEffect;

@CardRegistration(set = "5ED", collectorNumber = "59")
@CardRegistration(set = "ICE", collectorNumber = "51")
public class Seraph extends Card {

    public Seraph() {
        // Flying is loaded from Scryfall. "Whenever a creature dealt damage by this creature this
        // turn dies, put that card onto the battlefield under your control at the beginning of the
        // next end step. Sacrifice the creature when you lose control of this creature."
        addEffect(EffectSlot.ON_DAMAGED_CREATURE_DIES,
                new RegisterDelayedReturnDamagedCreatureUnderControlEffect());
    }
}
