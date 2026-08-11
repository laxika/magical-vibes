package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

@CardRegistration(set = "M20", collectorNumber = "145")
public class Infuriate extends Card {

    public Infuriate() {
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(3, 2));
    }
}
