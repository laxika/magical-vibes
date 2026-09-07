package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;

@CardRegistration(set = "ALA", collectorNumber = "80")
@CardRegistration(set = "ONS", collectorNumber = "157")
public class Infest extends Card {

    public Infest() {
        // All creatures get -2/-2 until end of turn.
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(-2, -2));
    }
}
