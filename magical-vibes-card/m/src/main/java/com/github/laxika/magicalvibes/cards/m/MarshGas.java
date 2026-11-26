package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;

@CardRegistration(set = "4ED", collectorNumber = "146")
@CardRegistration(set = "DRK", collectorNumber = "48")
public class MarshGas extends Card {

    public MarshGas() {
        // All creatures get -2/-0 until end of turn.
        addSpellEffect(BoostEffect.allCreatures(-2, 0));
    }
}
