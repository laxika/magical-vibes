package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Retrace;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;

@CardRegistration(set = "OTC", collectorNumber = "28")
@CardRegistration(set = "OTC", collectorNumber = "64")
public class EmbraceTheUnknown extends Card {

    public EmbraceTheUnknown() {
        addEffect(EffectSlot.SPELL, new ExileTopCardsMayPlayUntilNextTurnEffect(2));
        addCastingOption(new Retrace());
    }
}
