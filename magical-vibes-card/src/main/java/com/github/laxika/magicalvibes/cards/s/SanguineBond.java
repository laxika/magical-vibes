package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeEqualToLifeGainedEffect;

@CardRegistration(set = "M10", collectorNumber = "111")
public class SanguineBond extends Card {

    public SanguineBond() {
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE, new TargetPlayerLosesLifeEqualToLifeGainedEffect());
    }
}
