package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "BLB", collectorNumber = "39")
public class WaxWaneWitness extends Card {

    public WaxWaneWitness() {
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE, new BoostSelfEffect(1, 0));
        addEffect(EffectSlot.ON_CONTROLLER_LOSES_LIFE, new BoostSelfEffect(1, 0));
    }
}
