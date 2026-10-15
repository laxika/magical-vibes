package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;

@CardRegistration(set = "DFT", collectorNumber = "126")
public class FuelTheFlames extends Card {

    public FuelTheFlames() {
        addEffect(EffectSlot.SPELL, new MassDamageEffect(2));
        addCycling("{2}");
    }
}
