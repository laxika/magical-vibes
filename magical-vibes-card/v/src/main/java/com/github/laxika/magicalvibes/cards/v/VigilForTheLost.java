package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PayXManaGainXLifeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "SOM", collectorNumber = "26")
public class VigilForTheLost extends Card {

    public VigilForTheLost() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new PayXManaGainXLifeEffect());
    }
}
