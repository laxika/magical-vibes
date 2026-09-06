package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RampageEffect;

@CardRegistration(set = "5ED", collectorNumber = "344")
@CardRegistration(set = "LEG", collectorNumber = "214")
public class WolverinePack extends Card {

    public WolverinePack() {
        // Rampage 2: whenever Wolverine Pack becomes blocked, it gets +2/+2 until end of
        // turn for each creature blocking it beyond the first, i.e. 2 * (blockers - 1).
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new RampageEffect(2));
    }
}
