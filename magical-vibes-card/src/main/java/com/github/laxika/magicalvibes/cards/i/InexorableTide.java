package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;

@CardRegistration(set = "SOM", collectorNumber = "35")
public class InexorableTide extends Card {

    public InexorableTide() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new ProliferateEffect());
    }
}
