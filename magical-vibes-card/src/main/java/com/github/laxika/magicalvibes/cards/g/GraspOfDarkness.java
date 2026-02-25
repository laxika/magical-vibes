package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

@CardRegistration(set = "SOM", collectorNumber = "65")
public class GraspOfDarkness extends Card {

    public GraspOfDarkness() {
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(-4, -4));
    }
}
