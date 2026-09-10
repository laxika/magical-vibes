package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealFirstDrawCreatureEffect;

@CardRegistration(set = "SCG", collectorNumber = "126")
public class PrimitiveEtchings extends Card {

    public PrimitiveEtchings() {
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS, new RevealFirstDrawCreatureEffect());
    }
}
