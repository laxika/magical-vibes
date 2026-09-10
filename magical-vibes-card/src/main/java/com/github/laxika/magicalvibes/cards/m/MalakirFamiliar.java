package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "BFZ", collectorNumber = "116")
public class MalakirFamiliar extends Card {

    public MalakirFamiliar() {
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE, new BoostSelfEffect(1, 1));
    }
}
